import { describe, it, beforeEach, vi, expect } from 'vitest';
import HostCard from './HostCard.vue';
import { createStore } from 'vuex';
import { RouterLinkStub, mount } from '@vue/test-utils';

describe('HostCard component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { sendCommand: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
    });

    wrapper = mount(HostCard, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
      props: { host: { id: '12345678', clientName: 'TestHost', health: false, registered: true } },
    });
  });

  it('should render', async () => {
    expect(wrapper.vm).toBeDefined();
    await wrapper.setProps({
      host: { id: '12345678', clientName: 'TestHost', health: true, registered: true },
    });
    expect(wrapper.findAll('.footer span').at(0).text()).toStrictEqual('Online');
    await wrapper.setProps({
      host: { id: '12345678', clientName: 'TestHost', health: true, registered: false },
    });
    expect(wrapper.findAll('.footer span').at(1).text()).toStrictEqual('Unknown');
  });
});
