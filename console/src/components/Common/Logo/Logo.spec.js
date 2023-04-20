import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Logo from '../Logo/Logo.vue';
import { themes } from '@/store/constants';

describe('Logo component', () => {
  let wrapper, mockStore;
  beforeEach(() => {
    mockStore = createStore({
      getters: { theme: vi.fn(() => themes.LIGHT) },
    });

    wrapper = mount(Logo, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: {
        alt: 'Test logo',
        name: 'rmac-logo-combined',
        animated: true,
        config: { maxHeight: '2rem' },
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should render non-animated logo', () => {
    wrapper = mount(Logo, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: {
        alt: 'Test logo',
        name: 'rmac-logo-combined',
        animated: false,
        config: { maxHeight: '2rem' },
      },
    });

    expect(wrapper.vm).toBeDefined();
  });
});
