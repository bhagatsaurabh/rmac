import { describe, expect, it, vi } from 'vitest';
import router from './router';
import store from './store';
import bus from './event';
import { mutationKeys, notifications } from './store/constants';
import * as skt from './socket';

vi.mock('./socket', async () => {
  const socket = await vi.importActual('./socket');
  return { ...socket };
});
vi.mock('./store', () => ({
  default: {
    commit: vi.fn(),
    dispatch: vi.fn(),
    state: { bridge: { connected: false } },
    getters: { getHostById: vi.fn(() => ({ clientName: 'Test Host' })) },
  },
}));
vi.mock('./router', () => ({ default: { push: vi.fn() } }));
vi.mock('./event', () => ({
  default: {
    emit: vi.fn(),
  },
}));

describe('Socket comms', () => {
  it('should fail when connection to websocket server fails', async () => {
    const error = new Error('connection failed');
    window.WebSocket = function () {
      throw error;
    };

    await expect(skt.connect()).rejects.toThrowError(error);
    expect(store.commit).toHaveBeenCalledWith(mutationKeys.SET_STATUS_MSG, 'Connecting...');
  });

  it('should succeed when connection to websocket server succeeds and first message is recevied', async () => {
    const socket = { addEventListener: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    delete window.location;
    window.location = { protocol: 'https:' };

    vi.spyOn(store, 'dispatch').mockImplementation(() => true);

    setTimeout(() => {
      skt.onMessage({ data: JSON.stringify({ event: 'ack' }) });
    }, 100);
    await expect(skt.connect()).resolves.not.toThrow();
    expect(store.commit).toHaveBeenCalledWith(mutationKeys.SET_STATUS_MSG, 'Connecting...');
    expect(socket.addEventListener.mock.calls[0][0]).toStrictEqual('open');
    expect(socket.addEventListener.mock.calls[1][0]).toStrictEqual('close');
    expect(socket.addEventListener.mock.calls[2][0]).toStrictEqual('error');
    expect(socket.addEventListener.mock.calls[3][0]).toStrictEqual('message');
  });

  it('should fail when connection to websocket server succeeds but fetching hosts fails', async () => {
    const socket = { addEventListener: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';

    vi.spyOn(store, 'dispatch').mockImplementation(() => false);

    setTimeout(() => {
      skt.onMessage({ data: JSON.stringify({ event: 'ack' }) });
    }, 100);
    await expect(skt.connect()).rejects.toThrow('Failed to fetch hosts');
    expect(store.commit).toHaveBeenCalledWith(mutationKeys.SET_STATUS_MSG, 'Connecting...');
    expect(socket.addEventListener.mock.calls[0][0]).toStrictEqual('open');
    expect(socket.addEventListener.mock.calls[1][0]).toStrictEqual('close');
    expect(socket.addEventListener.mock.calls[2][0]).toStrictEqual('error');
    expect(socket.addEventListener.mock.calls[3][0]).toStrictEqual('message');
  });

  it('should close connection when disconnecting', async () => {
    const socket = { addEventListener: vi.fn(), close: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';

    vi.spyOn(store, 'dispatch').mockImplementation(() => true);

    setTimeout(() => {
      skt.onMessage({ data: JSON.stringify({ event: 'ack' }) });
    }, 100);

    await expect(skt.connect()).resolves.not.toThrow();

    skt.disconnect();

    expect(socket.close).toHaveBeenCalled();
  });

  it('should navigate to home page when connection closes', async () => {
    const socket = { addEventListener: vi.fn(), removeEventListener: vi.fn(), close: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';
    setTimeout(skt.onClose, 100);

    await expect(skt.connect()).rejects.toThrow('Connection closed');
    expect(socket.removeEventListener.mock.calls[0][0]).toStrictEqual('open');
    expect(socket.removeEventListener.mock.calls[1][0]).toStrictEqual('close');
    expect(socket.removeEventListener.mock.calls[2][0]).toStrictEqual('error');
    expect(socket.removeEventListener.mock.calls[3][0]).toStrictEqual('message');
    expect(router.push).toHaveBeenCalledWith('/');
  });

  it('should fail connection when in error while connecting', async () => {
    const socket = { addEventListener: vi.fn(), removeEventListener: vi.fn(), close: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';
    const error = new Error('Test Error');
    setTimeout(() => skt.onError(error), 100);

    await expect(skt.connect()).rejects.toThrowError(error);
  });

  it("should send 'identity' signal when connection opens", async () => {
    const socket = { addEventListener: vi.fn(), send: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';

    vi.spyOn(store, 'dispatch').mockImplementation(() => true);

    setTimeout(() => {
      skt.onMessage({ data: JSON.stringify({ event: 'ack' }) });
    }, 100);
    await expect(skt.connect()).resolves.not.toThrow();

    skt.onOpen();

    const message = { event: 'identity', type: 'console' };

    expect(socket.send).toHaveBeenCalledWith(JSON.stringify(message));
  });

  it('should navigate to home page when heartbeat stops for more than 31 seconds', async () => {
    vi.useFakeTimers();

    skt.heartbeat();

    vi.advanceTimersByTime(31000);

    expect(router.push).toHaveBeenCalledWith('/');

    vi.useRealTimers();
  });

  it('should process heartbeat signal correctly', async () => {
    const socket = { addEventListener: vi.fn(), send: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';
    vi.spyOn(store, 'dispatch').mockImplementation(() => true);
    setTimeout(() => skt.onMessage({ data: JSON.stringify({ event: 'ack' }) }), 100);

    await expect(skt.connect()).resolves.not.toThrow();

    await skt.onMessage({ data: '?' });

    expect(socket.send).toHaveBeenCalledWith('?');
  });

  it('should process health signal correctly', async () => {
    const socket = { addEventListener: vi.fn(), send: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';
    vi.spyOn(store, 'dispatch').mockImplementation(() => true);
    setTimeout(() => skt.onMessage({ data: JSON.stringify({ event: 'ack' }) }), 100);

    await expect(skt.connect()).resolves.not.toThrow();

    await skt.onMessage({ data: JSON.stringify({ event: 'health', data: { xyz: 'abc' } }) });

    expect(store.commit).toHaveBeenCalledWith(mutationKeys.SET_HOST_HEALTH, { xyz: 'abc' });
  });

  it('should process config signal correctly', async () => {
    const socket = { addEventListener: vi.fn(), send: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';
    vi.spyOn(store, 'dispatch').mockImplementation(() => true);
    setTimeout(() => skt.onMessage({ data: JSON.stringify({ event: 'ack' }) }), 100);

    await expect(skt.connect()).resolves.not.toThrow();

    await skt.onMessage({ data: JSON.stringify({ event: 'config', data: { xyz: 'abc' } }) });

    expect(store.commit).toHaveBeenCalledWith(mutationKeys.SET_HOST_CONFIG, {
      event: 'config',
      data: { xyz: 'abc' },
    });
  });

  it('should process hostid signal correctly', async () => {
    const socket = { addEventListener: vi.fn(), send: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';
    vi.spyOn(store, 'dispatch').mockImplementation(() => true);
    setTimeout(() => skt.onMessage({ data: JSON.stringify({ event: 'ack' }) }), 100);

    await expect(skt.connect()).resolves.not.toThrow();

    router.currentRoute = { value: { fullPath: '/host/abc' } };

    await skt.onMessage({
      data: JSON.stringify({ event: 'hostid', data: { oldId: 'abc', newId: 'xyz' } }),
    });

    expect(store.commit).toHaveBeenCalledWith(mutationKeys.SET_HOST_ID, {
      oldId: 'abc',
      newId: 'xyz',
    });
    expect(router.push).toHaveBeenCalledWith({ name: 'dashboard' });
    expect(bus.emit).toHaveBeenCalledWith('notify', notifications.IHOST_ID_CHANGED('Test Host'));
  });

  it('should process terminal:data signal correctly', async () => {
    const socket = { addEventListener: vi.fn(), send: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';
    vi.spyOn(store, 'dispatch').mockImplementation(() => true);
    setTimeout(() => skt.onMessage({ data: JSON.stringify({ event: 'ack' }) }), 100);

    await expect(skt.connect()).resolves.not.toThrow();

    await skt.onMessage({
      data: JSON.stringify({ event: 'terminal:data', rayId: 'abcd-1234', data: 'Test data' }),
    });

    expect(bus.emit).toHaveBeenCalledWith('abcd-1234', 'Test data');
  });

  it('should process terminal:close signal correctly', async () => {
    const socket = { addEventListener: vi.fn(), send: vi.fn() };
    window.WebSocket = function () {
      return socket;
    };
    import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL = 'test.com';
    vi.spyOn(store, 'dispatch').mockImplementation(() => true);
    setTimeout(() => skt.onMessage({ data: JSON.stringify({ event: 'ack' }) }), 100);

    await expect(skt.connect()).resolves.not.toThrow();

    await skt.onMessage({
      data: JSON.stringify({ event: 'terminal:close', rayId: 'abcd-1234' }),
    });

    expect(bus.emit).toHaveBeenCalledWith('terminal:close', 'abcd-1234');
  });
});
