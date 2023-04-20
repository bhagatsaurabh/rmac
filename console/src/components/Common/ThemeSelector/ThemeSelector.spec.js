import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import ThemeSelector from '../ThemeSelector/ThemeSelector.vue';
import { themes } from '@/store/constants';
import { nextTick } from 'vue';

describe('ThemeSelector component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { setTheme: vi.fn() };
    mockStore = createStore({
      modules: {},
      state: { preferences: { theme: themes.SYSTEM, sysTheme: themes.LIGHT } },
      mutations: { setTheme: (state, theme) => (state.preferences.theme = theme) },
      actions,
    });

    wrapper = mount(ThemeSelector, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should set correct theme', async () => {
    await wrapper.findAll('button').at(2).trigger('click');

    expect(actions.setTheme.mock.calls[0][1]).toStrictEqual(themes.DARK);
  });

  it('should close theme menu when current theme is already set', async () => {
    await wrapper.findAll('button').at(2).trigger('click');
    await wrapper.findAll('button').at(0).trigger('click');

    expect(actions.setTheme).toHaveBeenCalledTimes(1);
  });

  it('should close theme menu when active theme button loses focus', async () => {
    vi.useFakeTimers();

    await wrapper.findAll('button').at(2).trigger('click');
    await wrapper.findAll('button').at(0).trigger('blur');

    vi.advanceTimersByTime(100);

    expect(wrapper.vm.isOpen).toStrictEqual(false);
  });

  it('should move selected theme to top of the list', async () => {
    expect(wrapper.vm.themesList).toStrictEqual([
      themes.SYSTEM,
      themes.LIGHT,
      themes.DARK,
      themes.HIGH_CONTRAST,
    ]);

    mockStore.commit('setTheme', themes.HIGH_CONTRAST);
    await nextTick();

    expect(wrapper.vm.themesList).toStrictEqual([
      themes.HIGH_CONTRAST,
      themes.LIGHT,
      themes.DARK,
      themes.SYSTEM,
    ]);
  });
});
