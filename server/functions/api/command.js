import { db } from '../config/firebase.js';
import { APIError, errors } from "../middleware/error-handler.js";

const getCommand = async (req, res, next) => {
    try {
        if (!req.query.id) {
            throw new APIError(errors.CLIENT_ID_NOT_PROVIDED);
        }

        const dataSnap = await db.ref(req.query.id).get();
        if (dataSnap.exists()) {
            const commandsSnap = await db.ref(req.query.id).child('commands').get();
            if (commandsSnap.exists()) {
                const commands = commandsSnap.val();
                await db.ref(req.query.id).child('commands').set([]);
                return res.status(200).send(commands);
            } else {
                return res.status(200).send([]);
            }
        } else {
            throw new APIError(errors.CLIENT_ID_NOT_FOUND);
        }
    } catch (error) {
        next(error);
    }
};

const postCommand = async (req, res, next) => {
    try {
        if (!req.query.id) {
            throw new APIError(errors.CLIENT_ID_NOT_PROVIDED);
        }
        if (!Array.isArray(req.body)) {
            throw new APIError(errors.BAD_COMMANDS_FORMAT);
        }

        const dataSnap = await db.ref(req.query.id).get();
        if (dataSnap.exists()) {
            const commandsSnap = await db.ref(req.query.id).child('commands').get();
            let commands = commandsSnap.exists() ? commandsSnap.val() : [];
            commands = [...commands, ...req.body];
            await db.ref(req.query.id).child('commands').set(commands);
            return res.status(200).send('Commands updated');
        } else {
            throw new APIError(errors.CLIENT_ID_NOT_FOUND);
        }
    } catch (error) {
        next(error);
    }
};

export { getCommand, postCommand };