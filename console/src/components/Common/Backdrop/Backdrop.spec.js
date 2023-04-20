import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import Backdrop from '../Backdrop/Backdrop.vue';

describe('Backdrop component', () => {
  let wrapper;
  beforeEach(() => {
    wrapper = mount(Backdrop, {
      props: { show: false },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should dismiss when pointerdown event is triggered', async () => {
    wrapper = mount(Backdrop, {
      props: { show: true },
    });

    expect(wrapper.find('div.backdrop').exists()).toStrictEqual(true);

    await wrapper.find('div.backdrop').trigger('pointerdown');
  });
});
