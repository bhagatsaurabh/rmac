import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import TerminalNav from '../TerminalNav/TerminalNav.vue';

describe('TerminalNav component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { sendCommand: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
    });

    wrapper = mount(TerminalNav, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { terminals: ['abc1', 'lmn2', 'xyz3'] },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
    expect(wrapper.findAll('li:not(.add)').length).toStrictEqual(3);
  });

  it('should emit correct event when clicked on add icon', async () => {
    await wrapper.find('li.add button').trigger('click');

    expect(wrapper.emitted().add).toBeTruthy();
  });

  it('should emit correct event when a terminal is selected', async () => {
    await wrapper.findAll('li:not(.add) button').at(1).trigger('click');

    expect(wrapper.emitted().select[0]).toStrictEqual(['lmn2']);
  });

  it('should scroll option into view when updated', async () => {
    actions = { sendCommand: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
    });

    wrapper = mount(TerminalNav, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { terminals: ['abc1', 'lmn2', 'xyz3'] },
    });

    const scrollIntoView = vi.fn();
    document.querySelector = vi.fn(() => ({ scrollIntoView }));

    await wrapper.setProps({ terminals: ['xyz1', 'abc2', 'lmn3'], active: 'abc2' });

    expect(scrollIntoView).toHaveBeenCalled();
  });
});
