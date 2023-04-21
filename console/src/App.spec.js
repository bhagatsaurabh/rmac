import { describe, it, expect, vi, beforeEach } from 'vitest';
import { shallowMount } from '@vue/test-utils';
import { createStore } from 'vuex';
import App from './App.vue';
import { themes } from './store/constants';
import { nextTick } from 'vue';

let removeEventListener = vi.fn();
describe('App component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    window.matchMedia = vi.fn().mockImplementation(() => ({
      removeEventListener,
      addEventListener: vi.fn(),
    }));
    actions = { loadPreferences: vi.fn(), setTheme: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
      state: { preferences: { theme: themes.SYSTEM } },
    });

    wrapper = shallowMount(App, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
        stubs: ['RouterView'],
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should set theme when display media changes', async () => {
    wrapper.vm.mediaChangeHandler({ matches: true });
    await nextTick();

    expect(actions.setTheme.mock.calls[0][1]).toStrictEqual(themes.SYSTEM);
  });

  it('should de-register media change listeners before unmounting', async () => {
    wrapper.unmount();

    expect(removeEventListener).toHaveBeenCalledTimes(3);
  });
});
