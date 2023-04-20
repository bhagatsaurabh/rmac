import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Footer from '../Footer/Footer.vue';

describe('Footer component', () => {
  let wrapper;

  it('should render', () => {
    wrapper = mount(Footer);

    expect(wrapper.vm).toBeDefined();
  });
});
