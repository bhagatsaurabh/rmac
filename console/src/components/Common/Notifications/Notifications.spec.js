import { describe, it, expect, beforeEach, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Notifications from '../Notifications/Notifications.vue';
import { notificationTypes } from '@/store/constants';
import { createRouter, createWebHistory } from 'vue-router';
import Button from '../Button/Button.vue';
import { nextTick } from 'vue';
import Backdrop from '../Backdrop/Backdrop.vue';
import Icon from '../Icon/Icon.vue';
import bus from '@/event';
import Notification from '../Notification/Notification.vue';
import Toast from '../Toast/Toast.vue';

describe('Notifications component', () => {
  let wrapper, actions, mockStore, mockRouter;
  beforeEach(() => {
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });
    mockRouter.back = vi.fn();
    mockRouter.push = vi.fn();
    const nextFn = vi.fn();
    mockRouter.beforeEach = vi.fn((guard) => guard(null, { hash: '#notifications' }, nextFn));

    actions = {
      pushNotification: vi.fn(),
      readAllNotifications: vi.fn(),
    };
    mockStore = createStore({
      state: {
        notifications: {
          data: [
            {
              type: notificationTypes.ERROR,
              title: 'Test Error Notification',
              desc: 'Test Error Notification Description',
            },
            {
              type: notificationTypes.INFO,
              title: 'Test Info Notification',
              desc: 'Test Info Notification Description',
            },
            {
              type: notificationTypes.SUCCESS,
              title: 'Test Success Notification',
              desc: 'Test Success Notification Description',
            },
          ],
        },
      },
      actions,
    });

    wrapper = mount(Notifications, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
    expect(wrapper.findAllComponents(Notification).length).toStrictEqual(3);
  });

  it('should render when notifications are null', () => {
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });
    mockRouter.back = vi.fn();
    mockRouter.push = vi.fn();
    const nextFn = vi.fn();
    mockRouter.beforeEach = vi.fn((guard) => guard(null, { hash: '#notifications' }, nextFn));

    mockStore = createStore({
      state: {
        notifications: { data: null },
      },
      actions: {
        pushNotification: vi.fn(),
        readAllNotifications: vi.fn(),
      },
    });

    wrapper = mount(Notifications, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
      },
    });

    expect(wrapper.vm).toBeDefined();
  });

  it('should open when button is pressed', async () => {
    expect(wrapper.find('aside').classes().includes('open')).toStrictEqual(false);

    wrapper.findComponent(Button).vm.$emit('click');
    await nextTick();

    expect(wrapper.find('aside').classes().includes('open')).toStrictEqual(true);
  });

  it('should navigate back when backdrop is dismissed', async () => {
    wrapper.findComponent(Button).vm.$emit('click');
    await nextTick();

    wrapper.findComponent(Backdrop).vm.$emit('dismiss');
    await nextTick();

    expect(mockRouter.back).toHaveBeenCalled();
  });

  it('should navigate back when close icon is pressed', async () => {
    wrapper.findComponent(Button).vm.$emit('click');
    await nextTick();

    wrapper.find('aside').findComponent(Icon).vm.$emit('click');
    await nextTick();

    expect(mockRouter.back).toHaveBeenCalled();
  });

  it('should show toast notification when a new notification is received', async () => {
    expect(wrapper.find('aside').classes().includes('open')).toStrictEqual(false);

    bus.emit('notify', {
      type: notificationTypes.WARN,
      title: 'Test Warn Notification',
      desc: 'Test Warn Notification Description',
    });

    expect(actions.pushNotification.mock.calls[0][1]).toStrictEqual({
      type: notificationTypes.WARN,
      title: 'Test Warn Notification',
      desc: 'Test Warn Notification Description',
    });
    expect(wrapper.findComponent(Toast)).toBeDefined();
  });

  it('should open notifications sidebar when clicked on toast notification', async () => {
    expect(wrapper.find('aside').classes().includes('open')).toStrictEqual(false);

    bus.emit('notify', {
      type: notificationTypes.WARN,
      title: 'Test Warn Notification',
      desc: 'Test Warn Notification Description',
    });

    expect(actions.pushNotification.mock.calls[0][1]).toStrictEqual({
      type: notificationTypes.WARN,
      title: 'Test Warn Notification',
      desc: 'Test Warn Notification Description',
    });
    await flushPromises();
    expect(wrapper.find('.toast em').text()).toStrictEqual('Test Warn Notification');

    wrapper.findComponent(Toast).vm.$emit('click');
    await nextTick();

    expect(wrapper.find('aside').classes().includes('open')).toStrictEqual(true);
    await flushPromises();
    expect(wrapper.findComponent(Toast).exists()).toStrictEqual(false);
  });
});
