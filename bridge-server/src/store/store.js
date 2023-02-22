import { db } from "../config/firebase.js";

const state = {
  hosts: {},
  consoles: {},
  configs: {},
};

const addHost = (id, socket) => {
  state.hosts[id] = socket;
};
const removeHost = (id) => {
  delete state.hosts[id];
};
const addConsole = (id, socket) => {
  state.consoles[id] = socket;
};
const removeConsole = (id) => {
  delete state.consoles[id];
};
const getHosts = async () => {
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

  return Object.keys(hosts).map((id) => ({ ...hosts[id], id }));
};

export { state, addHost, removeHost, addConsole, removeConsole, getHosts };
