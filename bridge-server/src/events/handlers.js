import { state } from "../store/store.js";
import { v4 as uuid } from "uuid";
import { db } from "../config/firebase.js";

const emit = (socket, event, cId, hId, data) => {
  socket?.send(JSON.stringify({ event, cId, hId, data }));
};
const parse = (res) => {
  return JSON.parse(res);
};

const identity = async (socket, message) => {
  if (message.type === "host") {
    socket.id = message.hId;
    socket.type = message.type;

    state.hosts[socket.id] = socket;
    state.configs[socket.id] = message.data;
    console.log("Host connected:", socket.id);

    Object.keys(state.consoles).forEach((socketId) => {
      emit(state.consoles[socketId], "health", null, null, {
        [socket.id]: true,
      });
    });
  } else if (message.type === "console") {
    socket.id = uuid();
    socket.type = message.type;

    state.consoles[socket.id] = socket;
    console.log("Console connected:", socket.id);

    let hosts = {};
    Object.keys(state.hosts).forEach(
      (socketId) => (hosts[socketId] = { health: true, registered: false })
    );

    const snap = await db.ref().once("value");
    if (snap.exists()) {
      const registeredHosts = snap.val();
      Object.keys(registeredHosts).forEach((id) => {
        if (hosts[id]) {
          hosts[id] = { ...hosts[id], ...registeredHosts[id], registered: true };
        } else {
          hosts[id] = { ...registeredHosts[id], health: false, registered: true };
        }
      });
    }

    hosts = Object.keys(hosts).map((id) => ({ ...hosts[id], id }));

    emit(socket, "hosts", null, null, hosts);
  }
};

const config = (socket, message) => {
  if (socket.type === "host") {
    emit(state.consoles[message.cId], "config", socket.id, message.hId, message.data);
  } else if (socket.type === "console") {
    Object.keys(state.hosts).forEach((socketId) => {
      emit(state.hosts[socketId], "config", socket.id, null, null);
    });
  }
};

export { emit, parse, identity, config };
