import { apiURL, mutationKeys, notifications } from '@/store/constants';
import bus from '@/event';
import simulatedHosts from '@/assets/simulated-hosts.json';
import { timeout, timeoutFn, rand, defaultHeaders } from '@/utils';
import { emit, onMessage } from '@/socket';

const state = () => ({
  hosts: [],
  filteredHosts: [],
});

const mutations = {
  [mutationKeys.SET_HOST_HEALTH]: (state, data) => {
    const updatedHosts = [...state.hosts];

    const host = updatedHosts.find((host) => host.id === data.id);
    if (host) {
      host.health = data.health;
    }

    state.hosts = updatedHosts;
  },
  [mutationKeys.SET_HOSTS]: (state, data) => {
    state.hosts = data ?? [];
  },
  [mutationKeys.SET_FILTERED_HOSTS]: (state, data) => {
    state.filteredHosts = data ?? [];
  },
  [mutationKeys.SET_HOST_CONFIG]: (state, { id, data }) => {
    const updatedHosts = [...state.hosts];

    const host = updatedHosts.find((host) => host.id === id);
    if (host) {
      host.config = data;
      host.clientName = host.config.clientName;
      host.hostName = host.config.hostName;
    }

    state.hosts = updatedHosts;
  },
  [mutationKeys.SET_HOST_ID]: (state, { oldId, newId }) => {
    const updatedHosts = [...state.hosts];

    const host = updatedHosts.find((host) => host.id === oldId);
    if (host) {
      host.id = newId;
      host.registered = true;
    }

    state.hosts = updatedHosts;
  },
  [mutationKeys.ADD_SIMULATED_HOSTS]: (state) => {
    const updatedHosts = [...state.hosts];
    updatedHosts.push(...simulatedHosts);
    state.hosts = updatedHosts;
  },
};

const actions = {
  async fetchHosts({ commit, dispatch }) {
    try {
      const data = await (await fetch(`${apiURL}/hosts`)).json();
      commit(mutationKeys.SET_HOSTS, data);
    } catch (error) {
      bus.emit('notify', { ...notifications.EFETCH_HOSTS_FAILED(), desc: error.message });
      return false;
    }
    await dispatch('addSimulatedHosts');
    return true;
  },
  async addSimulatedHosts({ commit }) {
    commit(mutationKeys.ADD_SIMULATED_HOSTS);
  },
  async filter({ commit }, { config, hosts }) {
    let output = [...hosts];

    output = output
      .filter(
        (host) =>
          config.name === '' || host.clientName.toLowerCase().includes(config.name.toLowerCase())
      )
      .filter(
        (host) =>
          config.filter.connection.length === 0 ||
          config.filter.connection.includes(host.health ? 'online' : 'offline')
      )
      .filter(
        (host) =>
          config.filter.registration.length === 0 ||
          config.filter.registration.includes(host.registration ? 'registered' : 'unknown')
      )
      .filter((host) => {
        if (host.id.startsWith('sim-')) {
          return config.simulated;
        }
        return true;
      });

    if (config.sort.type === 'name') {
      output.sort((a, b) =>
        config.sort.order
          ? a.clientName.localeCompare(b.clientName)
          : b.clientName.localeCompare(a.clientName)
      );
    } else if (config.sort.type === 'connection') {
      output.sort((a, b) =>
        config.sort.order
          ? (a.health ? 'online' : 'offline').localeCompare(b.health ? 'online' : 'offline')
          : (b.health ? 'online' : 'offline').localeCompare(a.health ? 'online' : 'offline')
      );
    } else if (config.sort.type === 'registration') {
      output.sort((a, b) =>
        config.sort.order
          ? (a.registered ? 'registered' : 'unknown').localeCompare(
              b.registered ? 'registered' : 'unknown'
            )
          : (b.registered ? 'registered' : 'unknown').localeCompare(
              a.registered ? 'registered' : 'unknown'
            )
      );
    }

    commit(mutationKeys.SET_FILTERED_HOSTS, output);
  },
  async fetchConfig({ commit }, id) {
    if (id.startsWith('sim-')) {
      await timeout(1500);
      return true;
    }

    try {
      const res = await fetch(`${apiURL}/hosts/${id}/config`);
      if (res.status !== 204) {
        const data = await res.json();
        commit(mutationKeys.SET_HOST_CONFIG, { id, data });
      }
    } catch (error) {
      bus.emit('notify', { ...notifications.EFETCH_HOST_CONFIG_FAILED(), desc: error.message });
      return false;
    }
    return true;
  },
  async updateProperty({ getters }, { id, prop }) {
    const host = getters.getHostById(id);

    if (!host.health) {
      bus.emit('notify', notifications.WHOST_OFFLINE(host.clientName));
      return false;
    }

    if (host.id.startsWith('sim-')) {
      await timeout(200);
      timeoutFn(
        () =>
          onMessage({
            data: JSON.stringify({
              event: 'config',
              id,
              data: { ...host.config, [prop.name]: prop.value },
            }),
          }),
        rand(1000, 3000)
      );
      return true;
    }

    try {
      await fetch(`${apiURL}/hosts/${id}/property`, {
        method: 'POST',
        headers: defaultHeaders(),
        body: JSON.stringify(prop),
      });
    } catch (error) {
      bus.emit(
        'notify',
        notifications.EUPDATE_HOST_PROP_FAILED(host.clientName, prop.name, prop.value)
      );
      return false;
    }
    return true;
  },
  async openTerminal(_, { hostId, terminalId }) {
    if (hostId.startsWith('sim-')) {
      return;
    }
    emit({ event: 'terminal:open', type: 'console', data: null, rayId: `${hostId}:${terminalId}` });
  },
  async closeTerminal(_, { hostId, terminalId }) {
    if (hostId.startsWith('sim-')) {
      return;
    }

    emit({
      event: 'terminal:close',
      type: 'console',
      data: null,
      rayId: `${hostId}:${terminalId}`,
    });
  },
  async sendCommand({ state }, { hostId, command }) {
    if (hostId.startsWith('sim-')) {
      await timeoutFn(() => {
        bus.emit('notify', notifications.ISEND_COMMAND_SUCCESS());
      }, rand(1000, 2000));
      return;
    }

    if (state.hosts.find((host) => host.id === hostId)?.health) {
      emit({ event: 'command', type: 'console', data: command, rayId: hostId });
    } else {
      try {
        const res = await fetch(`${apiURL}/hosts/${hostId}/command`, {
          method: 'POST',
          body: JSON.stringify({ command }),
          headers: { 'Content-Type': 'application/json' },
        });
        if (res.status < 200 || res.status > 299) {
          throw await res.json();
        }
        bus.emit('notify', notifications.ISEND_COMMAND_SUCCESS());
      } catch (error) {
        bus.emit('notify', notifications.ESEND_COMMAND_FAILED(error));
      }
    }
  },
};

const getters = {
  getHostById: (state) => (id) => state.hosts.find((host) => host.id === id),
};

export default {
  state,
  mutations,
  actions,
  getters,
};
