import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Icon from '../Icon/Icon.vue';
import { themes } from '@/store/constants';

describe('Icon component', () => {
  let wrapper, getters, mockStore;
  beforeEach(() => {
    getters = { theme: vi.fn(() => themes.LIGHT) };
    mockStore = createStore({
      modules: {},
      getters,
    });

    wrapper = mount(Icon, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { alt: 'Test icon', name: 'icons/check' },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should update image source', () => {
    wrapper.setProps({ name: 'icons/bell' });

    expect(wrapper.vm).toBeDefined();
  });
});
