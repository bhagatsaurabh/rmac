import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import Modal from '../Modal/Modal.vue';
import { createRouter, createWebHistory } from 'vue-router';
import Backdrop from '../Backdrop/Backdrop.vue';
import { nextTick } from 'vue';
import Icon from '../Icon/Icon.vue';
import { createStore } from 'vuex';

describe('Modal component', () => {
  let wrapper, mockRouter, mockStore;

  beforeEach(() => {
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });
    mockRouter.push = vi.fn();
    mockRouter.back = vi.fn();
    mockStore = createStore({});

    wrapper = mount(Modal, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
      },
      props: {
        title: 'Test Modal',
        show: true,
      },
      slots: {
        default: 'Description for Test Modal',
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
    expect(mockRouter.push).toBeCalledWith({ hash: '#pop-test-modal' });
  });

  it('should navigate back when dismissing modal backdrop', async () => {
    wrapper.findComponent(Backdrop).vm.$emit('dismiss');
    await nextTick();

    expect(mockRouter.back).toBeCalled();
  });

  it('should navigate back when clicking on close icon', async () => {
    wrapper.findComponent(Icon).vm.$emit('click');
    await nextTick();

    expect(mockRouter.back).toBeCalled();
  });

  it('should unregister route guard when navigating away from modal', async () => {
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });
    mockRouter.push = vi.fn();
    mockRouter.back = vi.fn();
    const nextFn = vi.fn();
    mockRouter.beforeEach = vi.fn((guard) => guard(null, { hash: '#pop' }, nextFn));
    mockStore = createStore({});

    const unregisterGuard = vi.fn();

    wrapper = mount(Modal, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
        mocks: {
          unregisterGuard,
        },
      },
      props: {
        title: 'Test Modal',
        show: false,
      },
      slots: {
        default: 'Description for Test Modal',
      },
    });

    await wrapper.setProps({ show: true });

    expect(nextFn).toBeCalled();
  });

  it('should unregister blank route guard when modal not visible', async () => {
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });
    mockStore = createStore({});

    const unregisterGuard = vi.fn();

    wrapper = mount(Modal, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
        mocks: {
          unregisterGuard,
        },
      },
      props: {
        title: 'Test Modal',
        show: false,
      },
      slots: {
        default: 'Description for Test Modal',
      },
    });

    wrapper.unmount();
  });
});
