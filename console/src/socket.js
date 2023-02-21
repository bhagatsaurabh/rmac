import store from './store';
import { mutationKeys } from './store/constants';

let socket;
let pingTimer;
let connHandle = {};

const connect = () =>
  new Promise((resolve, reject) => {
    connHandle = {
      resolve: () => resolve() && (connHandle = {}),
      reject: (err) => reject(err) && (connHandle = {}),
    };
    pingTimer = -1;
    store.commit(mutationKeys.SET_STATUS_MSG, 'Connecting...');

    if (!import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL) {
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      socket = new WebSocket(`${protocol}//${window.location.host}`);
    } else {
      socket = new WebSocket(import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL);
    }

    addListeners();
  });

const disconnect = () => {
  socket?.close();
};

const onClose = () => {
  clearTimeout(pingTimer);
  if (!store.state.bridge.connected) {
    connHandle.reject?.();
  }
  removeListeners();
};
const onError = (err) => {
  console.log(err);
  if (!store.state.bridge.connected) {
    connHandle.reject?.(err);
  }
};
const onOpen = () => {
  heartbeat();
  store.commit(mutationKeys.SET_STATUS_MSG, 'Establishing identity...');
  emit({ event: 'identity', type: 'console' });
};
const onMessage = ({ data }) => {
  if (data === '?') {
    heartbeat();
    socket.send('?');
    return;
  }

  const message = JSON.parse(data);
  if (message.event === 'health') {
    store.commit(mutationKeys.SET_HOSTS_HEALTH, message);
  } else if (message.event === 'hosts') {
    store.commit(mutationKeys.SET_HOSTS, message.data);
    if (!store.state.bridge.connected) {
      connHandle.resolve?.();
    }
  } else if (message.event === 'config') {
    store.commit(mutationKeys.SET_HOST_CONFIG, message);
  }
};

const addListeners = () => {
  socket?.addEventListener('open', onOpen);
  socket?.addEventListener('close', onClose);
  socket?.addEventListener('error', onError);
  socket?.addEventListener('message', onMessage);
};
const removeListeners = () => {
  socket?.removeEventListener('open', onOpen);
  socket?.removeEventListener('close', onClose);
  socket?.removeEventListener('error', onError);
  socket?.removeEventListener('message', onMessage);
};

const emit = (message) => {
  socket.send(JSON.stringify(message));
};

const heartbeat = () => {
  clearTimeout(pingTimer);
  pingTimer = setTimeout(() => {
    socket.close();
  }, 30000 + 1000);
};

export { connect, disconnect, emit };

// socket.send(JSON.stringify({ event: 'config', type: 'console' }));
