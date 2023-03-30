import http from "http";
import { WebSocketServer } from "ws";
import staticServer from "./static.js";
import {
  config,
  parse,
  identity,
  hostid,
  terminalOpen,
  terminalData,
  terminalResize,
  terminalClose,
} from "../events/handlers.js";
import { onClose, onError } from "../events/listeners.js";

const server = http.createServer(staticServer);
const socketServer = new WebSocketServer({ server });

socketServer.on("connection", (socket) => {
  socket.isAlive = true;
  socket.on("pong", () => heartbeat(socket));

  socket.on("close", () => onClose(socket));
  socket.on("error", (err) => onError(socket, err));
  socket.on("message", async (rawData) => {
    if (rawData.toString() === "?") {
      heartbeat(socket);
      return;
    }

    const message = parse(rawData);

    if (message.event === "identity") {
      await identity(socket, message);
    } else if (message.event === "config") {
      config(socket, message);
    } else if (message.event === "hostid") {
      hostid(socket, message);
    } else if (message.event === "terminal:open") {
      terminalOpen(socket, message);
    } else if (message.event === "terminal:data") {
      terminalData(socket, message);
    } else if (message.event === "terminal:resize") {
      terminalResize(socket, message);
    } else if (message.event === "terminal:close") {
      terminalClose(socket, message);
    }
  });
});
socketServer.on("close", () => {
  clearInterval(pingTimer);
});

const heartbeat = (socket) => {
  socket.isAlive = true;
};
const pingTimer = setInterval(() => {
  socketServer.clients.forEach((socket) => {
    if (socket.isAlive === false) {
      return socket.terminate();
    }

    socket.isAlive = false;
    if (socket.type === "host") socket.ping();
    else if (socket.type === "console") socket.send("?");
  });
}, process.env.MAX_HEARTBEAT_THRESHOLD);

export default server;
