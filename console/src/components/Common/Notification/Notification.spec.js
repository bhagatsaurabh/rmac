import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Notification from '../Notification/Notification.vue';
import { notificationTypes } from '@/store/constants';

describe('Notification component', () => {
  let wrapper, mockStore;
  beforeEach(() => {
    mockStore = createStore({});

    wrapper = mount(Notification, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: {
        data: {
          type: notificationTypes.INFO,
          title: 'Test Notification',
          desc: 'Description for test notification',
        },
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
    expect(wrapper.findAll('.header span').at(1).text()).toStrictEqual('Test Notification');
    expect(wrapper.find('.content span').text()).toStrictEqual('Description for test notification');
  });
});
