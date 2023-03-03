import { apiURL, mutationKeys, notifications } from '@/store/constants';
import bus from '@/event';

const state = () => ({
  hosts: [],
  filteredHosts: [],
});

const mutations = {
  [mutationKeys.SET_HOSTS_HEALTH]: (state, message) => {
    const updatedHosts = { ...state.hosts };
    Object.keys(message).forEach((id) => {
      if (!updatedHosts[id]) {
        updatedHosts[id] = {};
      }
      updatedHosts[id].health = message[id];
    });
    state.hosts = updatedHosts;
  },
  [mutationKeys.SET_HOSTS]: (state, data) => {
    state.hosts = data ?? [];
  },
  [mutationKeys.SET_FILTERED_HOSTS]: (state, data) => {
    state.filteredHosts = data ?? [];
  },
  [mutationKeys.SET_HOST_CONFIG]: (state, message) => {
    const updatedHosts = [...state.hosts];
    updatedHosts[message.id] = message.data;
  },
};

const actions = {
  async fetchHosts({ commit }) {
    try {
      const data = await (await fetch(`${apiURL}/hosts`)).json();
      commit(mutationKeys.SET_HOSTS, data);
    } catch (error) {
      bus.emit('notify', { ...notifications.EFETCH_HOSTS_FAILED, desc: error.message });
      return false;
    }
    return true;
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
      );

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
          ? (a.registration ? 'registered' : 'unknown').localeCompare(
              b.registration ? 'registered' : 'unknown'
            )
          : (b.registration ? 'registered' : 'unknown').localeCompare(
              a.registration ? 'registered' : 'unknown'
            )
      );
    }

    commit(mutationKeys.SET_FILTERED_HOSTS, output);
  },
  async fetchConfig({ commit }, id) {
    try {
      const data = await (await fetch(`${apiURL}/config?id=${id}`)).json();
      commit(mutationKeys.SET_HOST_CONFIG, { id, data });
    } catch (error) {
      bus.emit('notify', { ...notifications.EFETCH_HOST_CONFIG_FAILED, desc: error.message });
      return false;
    }
    return true;
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
