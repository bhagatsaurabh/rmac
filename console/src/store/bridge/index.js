import { mutationKeys, notifications } from '@/store/constants';
import { connect, disconnect } from '@/socket';
import bus from '@/event';

const state = () => ({
  connected: false,
  pingTimer: -1,
  statusMsg: '',
});

const mutations = {
  [mutationKeys.SET_CONNECTED]: (state, connected) => {
    state.connected = connected;
  },
  [mutationKeys.SET_PING_TIMER]: (state, pingTimer) => {
    state.pingTimer = pingTimer;
  },
  [mutationKeys.SET_STATUS_MSG]: (state, msg) => {
    state.statusMsg = msg;
  },
};

const actions = {
  async connectToBridge({ state, commit }) {
    if (state.connected) return;

    try {
      await connect();
      commit(mutationKeys.SET_CONNECTED, true);
    } catch (error) {
      commit(mutationKeys.SET_CONNECTED, false);
      bus.emit('notify', notifications.ECONN_FAILED);
    } finally {
      commit(mutationKeys.SET_STATUS_MSG, '');
    }
  },
  async disconnectFromBridge({ state, commit }) {
    if (!state.connected) return;

    disconnect();
    commit(mutationKeys.SET_CONNECTED, false);
    bus.emit('notify', notifications.ICONN_DISCONNECTED);
  },
};

const getters = {};

export default {
  state,
  mutations,
  actions,
  getters,
};
