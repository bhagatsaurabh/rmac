import { db, bucket } from '../config/firebase.js';
import { APIError, errors } from "../middleware/error-handler.js";

const getUpdate = async (req, res, next) => {
    try {
        if (!req.query.version) {
            throw new APIError(errors.VERSION_NOT_PROVIDED);
        }

        const hostVersion = req.query.version;
        const latestHostVersion = (await db.ref('latestHostVersion').get()).val();
        const latestHostChecksum = (await db.ref('latestHostChecksum').get()).val();
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
        next(error);
    }
};

export { getUpdate };