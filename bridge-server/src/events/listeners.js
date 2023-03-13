import WebSocket from "ws";
import { removeConsole, removeHost, state } from "../store/store.js";
import { emit } from "./handlers.js";

const onClose = (socket) => {
  cleanUpSocket(socket);
  console.log("Socket closed: " + socket.id);
};

const onError = (socket, error) => {
  cleanUpSocket(socket);
  console.log("Socket error: " + socket.id);
};

const cleanUpSocket = (socket) => {
  if (!socket) return;

  if (socket.readyState !== WebSocket.CLOSED && socket.readyState !== WebSocket.CLOSING) {
    try {
      socket.terminate();
    } catch (error) {
      console.error("Error while closing socket: ", error);
    }
  }

  if (socket.type === "host") {
    Object.keys(state.consoles).forEach((cid) => {
      emit(state.consoles[cid], "health", null, null, { id: socket.id, health: false });
    });
    removeHost(socket.id);
  } else if (socket.type === "console") {
    removeConsole(socket.id);
  }
};

export { onClose, onError };
