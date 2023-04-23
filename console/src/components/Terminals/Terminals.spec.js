import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Terminals from './Terminals.vue';
import { Transition, nextTick } from 'vue';
import TerminalNav from '../Common/TerminalNav/TerminalNav.vue';
import { v4 as uuid } from 'uuid';
import bus from '@/event';
import Terminal from '../Common/Terminal/Terminal.vue';
import { notifications } from '@/store/constants';

vi.mock('xterm', () => ({
  Terminal: vi.fn(() => ({
    loadAddon: vi.fn(),
    onData: vi.fn(),
    onResize: vi.fn(),
    open: vi.fn(),
    write: vi.fn(),
    dispose: vi.fn(),
  })),
}));
vi.mock('xterm-addon-fit', () => ({
  FitAddon: vi.fn(() => ({
    fit: vi.fn(),
  })),
}));
vi.mock('uuid', () => ({
  v4: vi.fn(() => 'abcd1234'),
}));

describe('Terminals component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    window.ResizeObserver = class ResizeObserver {
      observe() {}
      unobserve() {}
    };
    actions = { sendCommand: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
    });

    wrapper = mount(Terminals, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { host: { id: '12345678', health: true } },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should open when button is pressed', async () => {
    expect(wrapper.vm.isOpen).toStrictEqual(false);

    await wrapper.find('aside button').trigger('click');

    expect(wrapper.vm.isOpen).toStrictEqual(true);
  });

  it('should add a default terminal when opened for the first time', async () => {
    const addSpy = vi.spyOn(wrapper.vm, 'handleAdd').mockImplementation(() => {});

    wrapper.findComponent(Transition).vm.$emit('enter');
    await nextTick();

    expect(addSpy).toHaveBeenCalled();
  });

  it('should handle terminal select', async () => {
    wrapper.findComponent(TerminalNav).vm.$emit('select', 'abcxyz');
    await nextTick();

    expect(wrapper.vm.activeTerminal).toStrictEqual('abcxyz');
  });

  it('should handle terminal add', async () => {
    wrapper.vm.terminals = ['123', 'lmn', 'xyz'];
    await nextTick();

    wrapper.findComponent(TerminalNav).vm.$emit('add');
    await nextTick();

    expect(uuid).toHaveBeenCalled();
    expect(wrapper.vm.terminals).toStrictEqual(['123', 'lmn', 'xyz', 'abcd1234']);
  });

  it('should handle terminal:close signal', async () => {
    bus.emit('terminal:close', '12345678:abcd');
  });

  it('should remove terminal correctly', async () => {
    bus.emit = vi.fn();

    wrapper.vm.terminals = ['abc', 'lmn', 'xyz'];
    await nextTick();

    wrapper.findAllComponents(Terminal).at(2).vm.$emit('close');
    await nextTick();

    expect(wrapper.vm.terminals).toStrictEqual(['abc', 'lmn']);
    expect(bus.emit).toHaveBeenCalledWith('notify', notifications.WTERMINAL_DISCONNECTED(3));
    expect(wrapper.vm.activeTerminal).toStrictEqual('abc');
  });
});
