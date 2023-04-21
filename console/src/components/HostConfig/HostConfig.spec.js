import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import HostConfig from './HostConfig.vue';
import bus from '../../event';
import { notifications } from '@/store/constants';

vi.mock('../../event', () => ({
  default: {
    emit: vi.fn(),
  },
}));

describe('HostConfig component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { fetchConfig: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
      getters: { getHostById: () => () => ({ config: { bridgeServerUrl: 'test.com' } }) },
    });

    wrapper = mount(HostConfig, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: {
        host: {
          id: '12345678',
          health: false,
          config: { id: '12345678', clientName: 'TestHost', bridgeServerUrl: 'test.com' },
        },
      },
    });
  });

  it('should render', async () => {
    expect(wrapper.vm).toBeDefined();
    await wrapper.setProps({
      host: {
        id: '12345678',
        health: true,
        config: { id: '12345678', clientName: 'TestHost', bridgeServerUrl: 'test.com' },
      },
    });
  });

  it('should notify when host becomes online', async () => {
    await wrapper.setProps({
      host: {
        id: '12345678',
        health: true,
        config: null,
      },
    });

    expect(bus.emit).toHaveBeenCalledWith('notify', notifications.IHOST_ONLINE());
    expect(actions.fetchConfig.mock.calls[0][1]).toStrictEqual('12345678');
  });
});
