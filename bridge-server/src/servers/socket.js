import http from 'http';
import { WebSocketServer } from 'ws';
import staticServer from "./static.js";
import { config, parse, identity } from '../events/handlers.js';
import { onClose, onError } from '../events/listeners.js';

const server = http.createServer(staticServer);
const socketServer = new WebSocketServer({ server });

/* const heartbeat = (socket) => { socket.isAlive = true; };
const interval = setInterval(() => {
    socketServer.clients.forEach((socket) => {
        if (socket.isAlive === false) {
            return socket.terminate();
        }

        socket.isAlive = false;
        socket.ping(() => ping(socket));
    })
}, process.env.MAX_HEARTBEAT_THRESHOLD); */

socketServer.on('connection', (socket) => {
    /* socket.isAlive = true;
    socket.on('pong', () => heartbeat(socket)); */

    socket.on('close', () => onClose(socket));
    socket.on('error', (err) => onError(socket, err));
    socket.on('message', (rawData) => {
        const message = parse(rawData);

        if (message.event === 'identity') {
            identity(socket, message);
        } else if (message.event === 'config') {
            config(socket, message);
        }
    });
});

export default server;
