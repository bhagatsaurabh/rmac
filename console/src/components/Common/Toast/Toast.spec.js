import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Toast from '../Toast/Toast.vue';
import { notificationTypes } from '@/store/constants';

describe('Toast component', () => {
  let wrapper, mockStore;
  beforeEach(() => {
    mockStore = createStore({});

    wrapper = mount(Toast, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: {
        data: {
          type: notificationTypes.WARN,
          title: 'Test Warn Notification',
          desc: 'Test Warn Notification Description',
        },
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });
});
