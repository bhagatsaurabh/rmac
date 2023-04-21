import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Host from './Host.vue';
import { createRouter, createWebHistory } from 'vue-router';

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

describe('Host component', () => {
  let wrapper, actions, mockStore, mockRouter;
  beforeEach(() => {
    window.ResizeObserver = class ResizeObserver {
      observe() {}
      unobserve() {}
    };
    actions = { sendCommand: vi.fn(), fetchConfig: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
      getters: { getHostById: () => () => ({ id: '12345678' }) },
    });
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });

    wrapper = mount(Host, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
        mocks: { route: { params: { hostid: '12345678' } } },
      },
      props: { host: { id: '12345678' } },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });
});
