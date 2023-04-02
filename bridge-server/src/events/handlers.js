import { v4 as uuid } from "uuid";
import { addConfig, addConsole, addHost, changeHostId, state } from "../store/store.js";

const emit = (socket, event, rayId, hId, data) => {
  socket?.send(JSON.stringify({ event, rayId, hId, data }));
};
const parse = (res) => {
  return JSON.parse(res);
};

const identity = async (socket, message) => {
  if (message.type === "host") {
    socket.id = message.hId || uuid();
    socket.type = message.type;

    addHost(socket.id, socket);
    addConfig(socket.id, message.data);
    console.log("Host connected:", socket.id);

    Object.keys(state.consoles).forEach((socketId) => {
      emit(state.consoles[socketId], "health", null, null, {
        id: socket.id,
        health: true,
      });
    });
  } else if (message.type === "console") {
    socket.id = uuid();
    socket.type = message.type;

    addConsole(socket.id, socket);
    console.log("Console connected:", socket.id);

    emit(socket, "ack", null, null, null);
  }
};
const config = (socket, message) => {
  if (socket.type === "host") {
    addConfig(socket.id, message.data);

    Object.keys(state.consoles).forEach((consoleId) => {
      emit(state.consoles[consoleId], "config", null, message.hId, message.data);
    });
  }
};
const hostid = (socket, message) => {
  if (socket.type === "host") {
    const oldId = socket.id;
    socket.id = message.data;
    changeHostId(oldId, socket.id);

    console.log("HostId Changed: ", { oldId, newId: socket.id });

    Object.keys(state.consoles).forEach((socketId) => {
      emit(state.consoles[socketId], "hostid", null, null, { oldId, newId: socket.id });
    });
  }
};
const terminalOpen = (socket, message) => {
  if (socket.type === "console") {
    const [hostId, terminalId] = message.rayId.split(":");
    emit(state.hosts[hostId], message.event, `${socket.id}:${terminalId}`, null, null);
  }
};
const terminalClose = (socket, message) => {
  if (socket.type === "host") {
    const [consoleId, terminalId] = message.rayId.split(":");
    emit(state.consoles[consoleId], message.event, `${socket.id}:${terminalId}`, null, null);
  } else {
    const [hostId, terminalId] = message.rayId.split(":");
    emit(state.hosts[hostId], message.event, `${socket.id}:${terminalId}`, null, null);
  }
};
const terminalData = (socket, message) => {
  if (socket.type === "host") {
    const [consoleId, terminalId] = message.rayId.split(":");
    emit(
      state.consoles[consoleId],
      message.event,
      `${socket.id}:${terminalId}`,
      null,
      message.data
    );
  } else {
    const [hostId, terminalId] = message.rayId.split(":");
    emit(state.hosts[hostId], message.event, `${socket.id}:${terminalId}`, null, message.data);
  }
};
const terminalResize = (socket, message) => {
  if (socket.type === "console") {
    const [hostId, terminalId] = message.rayId.split(":");
    emit(
      state.hosts[hostId],
      message.event,
      `${socket.id}:${terminalId}`,
      null,
      `${message.data.cols}:${message.data.rows}`
    );
  }
};
const command = (socket, message) => {
  if (socket.type === "console") {
    const hostId = message.rayId;
    emit(state.hosts[hostId], message.event, null, null, message.data);
  }
};

export {
  emit,
  parse,
  identity,
  config,
  hostid,
  terminalOpen,
  terminalClose,
  terminalData,
  terminalResize,
  command,
};
