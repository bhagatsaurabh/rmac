import { describe, expect, it, vi } from 'vitest';
import store from '@/store';
import { mutationKeys, notifications } from '../constants';
import * as skt from '@/socket';
import bus from '@/event';

vi.mock('@/socket', () => ({
  connect: vi.fn(),
  disconnect: vi.fn(),
}));
vi.mock('@/event', () => ({
  default: { emit: vi.fn() },
}));

describe('Bridge Store Mutations', () => {
  it('should set connected status', () => {
    store.commit(mutationKeys.SET_CONNECTED, true);

    expect(store.state.bridge.connected).toStrictEqual(true);
  });

  it('should set ping timer', () => {
    store.commit(mutationKeys.SET_PING_TIMER, 6373);

    expect(store.state.bridge.pingTimer).toStrictEqual(6373);
  });

  it('should set status message', () => {
    store.commit(mutationKeys.SET_STATUS_MSG, 'Test status message');

    expect(store.state.bridge.statusMsg).toStrictEqual('Test status message');
  });
});

describe('Bridge Store Actions', () => {
  it('should not connect to bridge server when already connected', async () => {
    store.state.bridge.connected = true;

    await store.dispatch('connectToBridge');

    expect(skt.connect).not.toHaveBeenCalled();
  });

  it('should notify when connection fails', async () => {
    store.state.bridge.connected = false;
    vi.spyOn(skt, 'connect').mockRejectedValue();

    await store.dispatch('connectToBridge');

    expect(bus.emit).toHaveBeenCalledWith('notify', notifications.ECONN_FAILED());
    expect(store.state.bridge.connected).toStrictEqual(false);
    expect(store.state.bridge.statusMsg).toStrictEqual('');
  });

  it('should connect successfully', async () => {
    store.state.bridge.connected = false;
    vi.spyOn(skt, 'connect').mockResolvedValue();

    await store.dispatch('connectToBridge');

    expect(store.state.bridge.connected).toStrictEqual(true);
    expect(store.state.bridge.statusMsg).toStrictEqual('');
  });

  it('should not disconnect when already disconnected', async () => {
    store.state.bridge.connected = false;

    await store.dispatch('disconnectFromBridge');

    expect(skt.disconnect).not.toHaveBeenCalled();
  });

  it('should disconnect and notify', async () => {
    store.state.bridge.connected = true;

    await store.dispatch('disconnectFromBridge');

    expect(skt.disconnect).toHaveBeenCalled();
    expect(store.state.bridge.connected).toStrictEqual(false);
    expect(bus.emit).toHaveBeenCalledWith('notify', notifications.WCONN_DISCONNECTED());
  });
});
