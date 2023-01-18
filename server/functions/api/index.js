import express from 'express';
import bodyParser from "body-parser";

import { errorHandler } from '../middleware/error-handler.js';
import { getRegister } from './register.js';
import { getCommand, postCommand } from './command.js';
import { getUpdate } from './update.js';

const getServer = (context) => {
    const server = express();

    server.use(bodyParser.json());
    server.use((req, _res, next) => {
        req.context = context;
        next();
    });

    server.get("/register", getRegister);
    server.get("/command", getCommand);
    server.post("/command", postCommand);
    server.get("/update", getUpdate);

    server.use(errorHandler);

    return server;
}

export { getServer };