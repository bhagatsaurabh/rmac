import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import HostDetails from './HostDetails.vue';

describe('HostDetails component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { sendCommand: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
      getters: { getHostById: () => () => ({ config: {} }) },
    });

    wrapper = mount(HostDetails, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { host: { id: '12345678', health: false, registered: true } },
    });
  });

  it('should render', async () => {
    expect(wrapper.vm).toBeDefined();
    await wrapper.setProps({ host: { id: '12345678', health: true, registered: true } });
    expect(wrapper.findAll('.status').at(0).findAll('span').at(2).text()).toStrictEqual('Online');
    await wrapper.setProps({ host: { id: '12345678', health: true, registered: false } });
    expect(wrapper.findAll('.status').at(1).findAll('span').at(2).text()).toStrictEqual('Unknown');
  });
});
