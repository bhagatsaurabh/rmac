import * as dotenv from 'dotenv'
dotenv.config({ path: `../.env.${process.env.NODE_ENV}` });

import { dirname, resolve } from 'path';
import { fileURLToPath } from 'url';
import express from 'express';
import http from 'http';
import { WebSocketServer } from 'ws';

const __dirname = dirname(fileURLToPath(import.meta.url));

const app = express();
app.use(express.static(resolve(__dirname, '../public')));
app.get('*', (_req, res) => {
    res.sendFile(resolve(__dirname, '../public/index.html'));
});

const server = http.createServer(app);
const wss = new WebSocketServer({ server });

wss.on('connection', (ws) => {
    ws.on('message', (message) => {
        ws.send(`Hello, you sent -> ${message}`);
    });

    ws.send('Hi there, I am a WebSocket server');
});

server.listen(process.env.PORT || 80, () => {
    console.log(`Bridge Server started on ${server.address().port}`);
});

/* const hosts = {};

net.createServer({ keepAlive: true }, (socket) => {
    socket.on('data', (res) => {
        const { event, data, id } = processResponse(res);
        handlers[event]?.(socket, data, id);
    });
    setupClosingHandlers(socket);
}).listen(process.env.SOUTHBOUND_INTERFACE_PORT);

const handlers = {
    ack: (socket, data) => {
        socket.id = data;
        socket.stream = new Readable();
        socket.stream._read = () => { };
        socket.stream.pipe(socket);

        hosts[data] = socket;
        console.log('Connected host: ', data);

        Object.keys(consoles).forEach(id => {
            consoles[id]?.emit('health', { data: { [socket.id]: true } });
        });
    },
    config: (socket, data, id) => {
        consoles[id]?.emit('config', { id: socket.id, data });
    }
};

const setupClosingHandlers = (socket) => {
    socket.on('close', () => {
        cleanUpSocket(socket);
        console.log('Socket closed: ' + socket.id);
    });
    socket.on('timeout', () => {
        cleanUpSocket(socket);
        console.log('Socket timed out: ' + socket.id);
    });
    socket.on('error', (err) => {
        cleanUpSocket(socket);
        console.log('Socket error: ' + socket.id);
        // console.error('Socket error: ', err);
    });
};

const processResponse = (res) => {
    const raw = res.toString();
    const semi = raw.indexOf(';');
    const colon = raw.indexOf(':');

    return {
        event: raw.slice(0, semi),
        data: raw.slice(colon + 1),
        id: raw.slice(semi + 1, colon)
    };
};

const cleanUpSocket = (s) => {
    if (!s) return;

    if (!s.closed) {
        if (s.stream) {
            s.stream.unpipe(s);
            delete s.stream;
        }
        try {
            s.destroy();
        } catch (error) {
            console.error('Error while destroying socket: ', error)
        }
    }
    Object.keys(consoles).forEach(id => {
        consoles[id]?.emit('health', { data: { [s.id]: false } });
    });
    delete hosts[s.id];
};

const app = express();
const consoleServer = http.createServer(app);
const io = new Server(consoleServer, {
    cors: { origin: "*" }
});

const consoles = {};

io.on('connection', (socket) => {
    console.log('Connected console: ', socket.id);
    consoles[socket.id] = socket;

    const healthStatus = {};
    Object.keys(hosts).forEach(id => healthStatus[id] = true);
    socket.emit('health', { data: healthStatus });

    socket.on('disconnect', () => {
        delete consoles[socket.id];
        console.log('Disconnected console: ', socket.id);
    });

    socket.on('config', () => {
        Object.keys(hosts).forEach(id => {
            hosts[id]?.stream.push('config:' + socket.id + '\n');
        });
    });
});

consoleServer.listen(process.env.PORT || process.env.SOUTHBOUND_INTERFACE_PORT);
 */