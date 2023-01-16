import express from 'express';
import bodyParser from "body-parser";

import { errorHandler } from './middleware/error-handler.js';
import { storage, https } from './config/firebase.js';
import { getRegister } from './api/register.js';
import { getCommand, postCommand } from './api/command.js';
import { getUpdate } from './api/update.js';
import { updaterFunction } from './updater/updater.js';

const server = express();

server.use(bodyParser.json());

server.get("/register", getRegister);
server.get("/command", getCommand);
server.post("/command", postCommand);
server.get("/update", getUpdate);

server.use(errorHandler);

export const api = https.onRequest(server);
export const rmacClientUpdater = storage.object().onFinalize(updaterFunction);
