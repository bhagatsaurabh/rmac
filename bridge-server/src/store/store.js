const state = {
    hosts: {},
    consoles: {}
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

export { state, addHost, removeHost, addConsole, removeConsole };
