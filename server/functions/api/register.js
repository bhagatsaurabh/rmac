import { db } from '../config/firebase.js';
import { APIError, errors } from '../middleware/error-handler.js';

const getRegister = async (req, res, next) => {
    try {
        if (!req.query.clientName || !req.query.hostName) {
            throw new APIError(errors.CLIENT_HOST_NAME_NOT_PROVIDED);
        }

        const requestData = req.query;
        if (!requestData.id) {
            const id = await register(requestData);
            return res.status(201).send(id);
        }

        const snapshot = await db.ref(requestData.id).once('value');
        if (snapshot.exists()) {
            return res.status(200).send(requestData.id);
        } else {
            const id = await register(requestData);
            return res.status(201).send(id);
        }
    } catch (error) {
        next(error);
    }
};

const register = async (data) => {
    const { clientName, hostName } = data;
    const newClient = await db.ref().push({ clientName, hostName });
    return newClient.key;
}

export { getRegister };