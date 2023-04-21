import { describe, it, expect, vi, beforeEach } from 'vitest';
import { RouterLinkStub, mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import HostList from './HostList.vue';
import HostCard from '../HostCard/HostCard.vue';

describe('HostList component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { sendCommand: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
    });

    wrapper = mount(HostList, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
      props: { hosts: [{ id: '123' }, { id: '456' }, { id: '789' }, { id: '012' }] },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
    expect(wrapper.findAllComponents(HostCard).length).toStrictEqual(4);
  });
});
