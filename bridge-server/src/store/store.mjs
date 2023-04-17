import { db } from "../config/firebase.mjs";

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
const addConfig = (id, data) => {
  state.configs[id] = data;
};
const removeConfig = (id) => {
  delete state.configs[id];
};
const changeHostId = (oldId, newId) => {
  const socket = state.hosts[oldId];
  addHost(newId, socket);
  removeHost(oldId);

  const data = state.configs[oldId];
  addConfig(newId, data);
  removeConfig(oldId);
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
const getRegisteredHost = async (id) => {
  const snap = await db.ref(id).once("value");
  if (snap.exists()) {
    return snap.val();
  }
  return null;
};

export {
  state,
  addHost,
  removeHost,
  addConsole,
  removeConsole,
  getHosts,
  addConfig,
  removeConfig,
  changeHostId,
  getRegisteredHost,
};
