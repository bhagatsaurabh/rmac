import { describe, it, expect } from 'vitest';
import { mount, shallowMount } from '@vue/test-utils';
import Button from '../Button/Button.vue';

describe('Button component', () => {
  let wrapper;

  it('should render', () => {
    wrapper = mount(Button, {
      props: {},
      slots: {
        default: 'Test Button',
      },
      global: {
        directives: { hide() {} },
      },
    });

    expect(wrapper.vm).toBeDefined();
    expect(wrapper.find('span').text()).toStrictEqual('Test Button');
  });

  it('should render button with right icon', () => {
    wrapper = shallowMount(Button, {
      props: { icon: 'copy', iconRight: true },
      slots: {
        default: 'Test Button',
      },
      global: {
        directives: { hide() {} },
      },
    });

    expect(wrapper.find('span').text()).toStrictEqual('Test Button');
  });
});
