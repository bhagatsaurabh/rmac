import store from './store';
import { mutationKeys } from './store/constants';

let socket;

const connect = () =>
  new Promise((resolve, reject) => {
    store.commit(mutationKeys.SET_STATUS_MSG, 'Connecting...');

    if (!import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL) {
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      socket = new WebSocket(`${protocol}//${window.location.host}`);
    } else {
      socket = new WebSocket(import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL);
    }

    const handleOpen = () => {
      store.commit(mutationKeys.SET_STATUS_MSG, 'Establishing identity...');
    };
    const handleMessage = () => {};
    const handleError = () => {};
    const handleClose = () => {};

    socket.addEventListener('open', handleOpen);
    socket.addEventListener('message', handleMessage);
    socket.addEventListener('error', handleError);
    socket.addEventListener('close', handleClose);
  });

const emit = (message) => {
  socket.send(JSON.stringify(message));
};

export { connect };

/* 
let pingTimer = -1;

let socket;
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
  setTimeout(() => {
    socket.send(JSON.stringify({ event: 'config', type: 'console' }));
  }, 5000);
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
*/
