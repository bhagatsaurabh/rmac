let admin = require("firebase-admin");
const { getStorage } = require('firebase-admin/storage');
const path = require('path');
const os = require('os');
const fs = require('fs');
const crypto = require('crypto');
const StreamZip = require('node-stream-zip');
const functions = require("firebase-functions");
const express = require("express");
const bodyParser = require('body-parser');

const adminConfig = JSON.parse(process.env.FIREBASE_CONFIG);
admin.initializeApp(adminConfig);
const database = admin.database();
const bucket = getStorage().bucket();

const server = express();
server.use(bodyParser.json());

server.get("/register", async (req, res) => {
    if (!req.query.clientName || !req.query.hostName) {
        return res.status(400).send('Error: ClientName and HostName must be provided');
    }
    const requestData = req.query;
    if (!requestData.id) {
        try {
            const id = await register(requestData);
            return res.status(201).send(id);
        } catch (error) {
            functions.logger.error(error);
            return res.status(500).send();
        }
    }

    try {
        const snapshot = await database.ref(requestData.id).once('value');
        if (snapshot.exists()) {
            return res.status(200).send(requestData.id);
        } else {
            const id = await register(requestData);
            return res.status(201).send(id);
        }
    } catch (error) {
        functions.logger.error(error);
        return res.status(500).send();
    }
});

server.get("/command", async (req, res) => {
    if (!req.query.id) {
        return res.status(400).send('Error: ClientId must be provided');
    }
    try {
        const dataSnap = await database.ref(req.query.id).get();
        if (dataSnap.exists()) {
            const commandsSnap = await database.ref(req.query.id).child('commands').get();
            if (commandsSnap.exists()) {
                const commands = commandsSnap.val();
                await database.ref(req.query.id).child('commands').set([]);
                return res.status(200).send(commands);
            } else {
                return res.status(200).send([]);
            }
        } else {
            return res.status(404).send('Error: ClientId Not Found');
        }
    } catch (error) {
        functions.logger.error(error);
        return res.status(500).send();
    }
});

server.post("/command", async (req, res) => {
    if (!req.query.id) {
        return res.status(400).send('Error: ClientId must be provided');
    }
    if (!Array.isArray(req.body)) {
        return res.status(400).send('Error: Bad commands format, must be an array of commands');
    }
    try {
        const dataSnap = await database.ref(req.query.id).get();
        if (dataSnap.exists()) {
            const commandsSnap = await database.ref(req.query.id).child('commands').get();
            let commands = commandsSnap.exists() ? commandsSnap.val() : [];
            commands = [...commands, ...req.body];
            await database.ref(req.query.id).child('commands').set(commands);
            return res.status(200).send('Commands updated');
        } else {
            return res.status(404).send('Error: ClientId Not Found');
        }
    } catch (error) {
        functions.logger.error(error);
        return res.status(500).send();
    }
});

server.get("/update", async (req, res) => {
    if (!req.query.version) {
        return res.status(400).send('Error: Version must be provided');
    }
    try {
        const hostVersion = req.query.version;
        const latestHostVersion = (await database.ref('latestHostVersion').get()).val();
        const latestHostChecksum = (await database.ref('latestHostChecksum').get()).val();
        if (hostVersion !== latestHostVersion) {
            const [downloadUrl] = await bucket.file(latestHostVersion + ".jar").getSignedUrl({
                action: "read",
                expires: Date.now() + 1000 * 60 * 30,
            })
            return res.status(200).send([downloadUrl, latestHostChecksum]);
        } else {
            return res.status(200).send([]);
        }
    } catch (error) {
        functions.logger.error(error);
        return res.status(500).send();
    }
});

const register = async (data) => {
    const { clientName, hostName } = data;
    const newClient = await database.ref().push({ clientName, hostName });
    return newClient.key;
}

exports.api = functions.https.onRequest(server);
exports.rmacClientUpdater = functions.storage.object().onFinalize(async (object) => {
    const filePath = object.name;
    const contentType = object.contentType;
    if (contentType !== 'application/octet-stream' || !filePath.endsWith('.jar')) {
        functions.logger.warn('Not a JAR: ' + contentType);
        return;
    }

    // Download new RMAC client JAR
    const fileName = path.basename(filePath);
    const tempFilePath = path.join(os.tmpdir(), fileName);
    await bucket.file(filePath).download({ destination: tempFilePath });
    functions.logger.log('New RMAC client downloaded locally');

    // Calculate SHA-256 Checksum
    const fileBuffer = fs.readFileSync(tempFilePath);
    const hash = crypto.createHash('sha256');
    hash.update(fileBuffer);
    const checksum = hash.digest('hex');

    // Read version from JAR manifest
    const zip = new StreamZip.async({
        file: tempFilePath,
        storeEntries: true
    });
    try {
        let manifest = (await zip.entryData('META-INF/MANIFEST.MF')).toString().split(/(?:\r\n|\r|\n)/g);
        await zip.close();

        let version = '';
        manifest.forEach(line => {
            if (line.split(':')[0].trim() === 'Version') {
                version = line.split(':')[1].trim();
            }
        });

        if (version !== '') {
            // Update Database with checksum & version
            functions.logger.log('New RMAC client: ' + version + ' ; ' + checksum);
            await database.ref().child('latestHostChecksum').set(checksum);
            await database.ref().child('latestHostVersion').set(version);
        } else {
            functions.logger.warn('Version is blank');
        }
    } catch (error) {
        functions.logger.error(error);
    } finally {
        fs.unlinkSync(tempFilePath);
        functions.logger.log('Locally downloaded RMAC client deleted');
    }
});