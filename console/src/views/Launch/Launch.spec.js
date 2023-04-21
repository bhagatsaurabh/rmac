import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Launch from './Launch.vue';
import { createRouter, createWebHistory } from 'vue-router';
import Button from '@/components/Common/Button/Button.vue';
import { nextTick } from 'vue';

describe('Launch component', () => {
  let wrapper, actions, mockStore, mockRouter;
  beforeEach(() => {
    actions = { connectToBridge: vi.fn(), disconnectFromBridge: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
      mutations: { setConnected: (state, status) => (state.bridge.connected = status) },
      state: { bridge: { statusMsg: 'Test Status', connected: false } },
    });
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });
    mockRouter.push = vi.fn();

    wrapper = mount(Launch, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should handle connection to bridge server', async () => {
    wrapper.find('.launch-right').findComponent(Button).vm.$emit('click');
    await nextTick();

    expect(actions.connectToBridge).toHaveBeenCalled();
  });

  it('should navigate to dashboard when connected', async () => {
    mockStore.commit('setConnected', true);
    await nextTick();

    expect(mockRouter.push).toHaveBeenCalledWith({ name: 'dashboard' });
  });

  it('should disconnect when already connected', async () => {
    wrapper.unmount();

    actions = { connectToBridge: vi.fn(), disconnectFromBridge: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
      state: { bridge: { statusMsg: 'Test Status', connected: true } },
    });
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });
    mockRouter.push = vi.fn();

    wrapper = mount(Launch, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
      },
    });

    expect(wrapper.vm).toBeDefined();
    expect(actions.disconnectFromBridge).toHaveBeenCalled();
  });
});
