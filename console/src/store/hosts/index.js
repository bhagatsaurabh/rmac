import { apiURL, mutationKeys, notifications } from '@/store/constants';
import bus from '@/event';

const state = () => ({
  hosts: [],
});

const mutations = {
  [mutationKeys.SET_HOSTS_HEALTH]: (state, data) => {
    const updatedHosts = { ...state.hosts };
    Object.keys(data).forEach((id) => {
      if (!updatedHosts[id]) {
        updatedHosts[id] = {};
      }
      updatedHosts[id].health = data[id];
    });
    state.hosts = updatedHosts;
  },
  [mutationKeys.SET_HOSTS]: (state, data) => {
    state.hosts = data ?? [];
  },
};

const actions = {
  async fetchHosts({ commit }) {
    try {
      const data = await (await fetch(apiURL)).json();
      commit(mutationKeys.SET_HOSTS, data);
    } catch (error) {
      bus.emit('notify', { ...notifications.EFETCH_HOSTS_FAILED, desc: error.message });
      return false;
    }
    return true;
  },
};

const getters = {};

export default {
  state,
  mutations,
  actions,
  getters,
};
