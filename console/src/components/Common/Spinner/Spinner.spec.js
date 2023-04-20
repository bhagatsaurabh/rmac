import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Spinner from '../Spinner/Spinner.vue';

describe('Spinner component', () => {
  let wrapper, mockStore;
  beforeEach(() => {
    mockStore = createStore({});

    wrapper = mount(Spinner, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });
});
