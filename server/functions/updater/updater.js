import * as path from 'path';
import * as os from 'os';
import { unlinkSync, readFileSync } from "fs";
import { createHash } from "crypto";
import * as StreamZip from "node-stream-zip";

import { db, logger, bucket } from '../config/firebase.js';

const updaterFunction = async (object) => {
    const filePath = object.name;
    const contentType = object.contentType;
    if (contentType !== 'application/octet-stream' || !filePath.endsWith('.jar')) {
        logger.warn('Not a JAR: ' + contentType);
        return;
    }

    // Download new RMAC client JAR
    const fileName = path.basename(filePath);
    const tempFilePath = path.join(os.tmpdir(), fileName);
    await bucket.file(filePath).download({ destination: tempFilePath });
    logger.log('New RMAC client downloaded locally');

    // Calculate SHA-256 Checksum
    const fileBuffer = readFileSync(tempFilePath);
    const hash = createHash('sha256');
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
            logger.log('New RMAC client: ' + version + ' ; ' + checksum);
            await db.ref().child('latestHostChecksum').set(checksum);
            await db.ref().child('latestHostVersion').set(version);
        } else {
            logger.warn('Version is blank');
        }
    } catch (error) {
        logger.error(error);
    } finally {
        unlinkSync(tempFilePath);
        logger.log('Locally downloaded RMAC client was deleted');
    }
};

export { updaterFunction };