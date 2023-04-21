import { describe, expect, it, vi } from 'vitest';
import router from '@/router';
import store from '@/store';

vi.mock('@/store', () => ({
  default: {
    state: { bridge: { connected: false } },
  },
}));
vi.mock('xterm', () => ({
  Terminal: vi.fn(() => ({
    loadAddon: vi.fn(),
    onData: vi.fn(),
    onResize: vi.fn(),
    open: vi.fn(),
    write: vi.fn(),
    dispose: vi.fn(),
  })),
}));
vi.mock('xterm-addon-fit', () => ({
  FitAddon: vi.fn(() => ({
    fit: vi.fn(),
  })),
}));

describe('The Router', () => {
  it('should set document title when navigation happens', async () => {
    await router.push('/');

    expect(document.title).toStrictEqual('RMAC');
  });

  it('should guard routes when connection to bridge server is not established', async () => {
    await router.push('/dashboard');

    expect(router.currentRoute.value.fullPath).toStrictEqual('/');

    store.state.bridge.connected = true;

    await router.push('/dashboard');

    expect(router.currentRoute.value.fullPath).toStrictEqual('/dashboard');
  });
});
