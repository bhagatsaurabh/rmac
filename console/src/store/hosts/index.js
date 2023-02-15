import { mutationKeys } from '@/store/constants';

const state = () => ({
  hosts: {},
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
};

const actions = {};

const getters = {};

export default {
  state,
  mutations,
  actions,
  getters,
};
