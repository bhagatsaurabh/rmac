import { mutationKeys } from '@/store/constants';

let socket;

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
  async connect({ state, commit, dispatch }) {
    if (state.connected) return;

    if (!import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL) {
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      socket = new WebSocket(`${protocol}//${window.location.host}`);
    } else {
      socket = new WebSocket(import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL);
    }

    socket.onclose = () => {
      console.log('Disconnected from bridging server');
      clearTimeout(pingTimer);
    };
    socket.onerror = (err) => {
      console.log(err);
    };

    socket.onopen = () => {
      console.log('Connected to bridging server');
      heartbeat();
      socket.send(JSON.stringify({ event: 'identity', type: 'console' }));
      /* setTimeout(() => {
        socket.send(JSON.stringify({ event: 'config', type: 'console' }));
      }, 5000); */
    };
    socket.onmessage = (messageEvent) => {
      if (messageEvent.data === '?') {
        heartbeat();
        socket.send('?');
        return;
      }

      const message = JSON.parse(messageEvent.data);
      if (message.event === 'health') {
        console.log(message);
      } else if (message.event === 'config') {
        console.log(message);
      }
    };

    const heartbeat = () => {
      clearTimeout(pingTimer);
      pingTimer = setTimeout(() => {
        socket.close();
      }, 30000 + 1000);
    };
  },
};

const getters = {};

export default {
  state,
  mutations,
  actions,
  getters,
};
