import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createRouter, createWebHistory } from 'vue-router';
import { createStore } from 'vuex';
import { nextTick } from 'vue';
import Info from '../Info/Info.vue';
import Modal from '../Modal/Modal.vue';

describe('Info component', () => {
  let wrapper, mockStore, mockRouter;
  beforeEach(() => {
    mockStore = createStore({});
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });
    mockRouter.push = vi.fn();

    wrapper = mount(Info, {
      props: { hideLabel: false },
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
      },
      slots: {
        title: 'Test infocon',
        desc: 'Description for test infocon',
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should open modal on small screens when clicked/touched', async () => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation(() => ({
        matches: true,
      })),
    });

    await wrapper.find('button.infocon-button').trigger('click');

    expect(wrapper.findComponent(Modal).exists()).toStrictEqual(true);
    expect(wrapper.find('.modal').exists()).toStrictEqual(true);
  });

  it('should close modal when dismissed', async () => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation(() => ({
        matches: true,
      })),
    });

    await wrapper.find('button.infocon-button').trigger('click');

    expect(wrapper.findComponent(Modal).exists()).toStrictEqual(true);
    expect(wrapper.find('.modal').exists()).toStrictEqual(true);

    wrapper.findComponent(Modal).vm.$emit('dismiss');
    await nextTick();

    expect(wrapper.find('.modal').exists()).toStrictEqual(false);
  });
});
