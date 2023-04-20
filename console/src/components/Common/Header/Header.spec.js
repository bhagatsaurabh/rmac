import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Header from '../Header/Header.vue';

describe('Header component', () => {
  let wrapper;

  it('should render', () => {
    wrapper = mount(Header, {
      global: {
        mocks: { route: { path: '' } },
      },
    });

    expect(wrapper.vm).toBeDefined();
  });
});
