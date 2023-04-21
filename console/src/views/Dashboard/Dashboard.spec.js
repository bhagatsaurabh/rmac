import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Dashboard from './Dashboard.vue';
import SearchFilter from '@/components/Common/SearchFilter/SearchFilter.vue';
import { nextTick } from 'vue';

describe('Dashboard component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { filter: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
      state: { hosts: { hosts: [], filteredHosts: [] } },
    });

    wrapper = mount(Dashboard, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should handle search & filter query', async () => {
    wrapper.findComponent(SearchFilter).vm.$emit('query', { xyz: 'abc' });
    await nextTick();

    expect(actions.filter.mock.calls[1][1]).toStrictEqual({
      config: { xyz: 'abc' },
      hosts: [],
    });
  });
});
