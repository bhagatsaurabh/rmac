import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Terminal from '../Terminal/Terminal.vue';
import { createRouter, createWebHistory } from 'vue-router';
import Button from '../Button/Button.vue';
import { nextTick } from 'vue';
import { emit } from '@/socket';
import bus from '@/event';

let onDataCallback, onResizeCallback;
const write = vi.fn();

vi.mock('xterm', () => ({
  Terminal: vi.fn(() => ({
    loadAddon: vi.fn(),
    onData: (callback) => (onDataCallback = callback),
    onResize: (callback) => (onResizeCallback = callback),
    open: vi.fn(),
    write,
    dispose: vi.fn(),
  })),
}));
vi.mock('xterm-addon-fit', () => ({
  FitAddon: vi.fn(() => ({
    fit: vi.fn(),
  })),
}));
vi.mock('@/socket', () => ({
  emit: vi.fn(),
}));

describe('Terminal component', () => {
  let wrapper, actions, mockStore, mockRouter;
  beforeEach(() => {
    window.ResizeObserver = class ResizeObserver {
      observe() {}
      unobserve() {}
    };
    actions = { openTerminal: vi.fn(), closeTerminal: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
    });
    mockRouter = createRouter({ history: createWebHistory(), routes: [] });

    wrapper = mount(Terminal, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
      },
      props: { id: 'abcd', host: { id: 'xyz' }, idx: 0 },
    });
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should emit correct event when disconnected', async () => {
    wrapper.findComponent(Button).vm.$emit('click');
    await nextTick();

    expect(wrapper.emitted().close).toBeTruthy();
  });

  it('should return correct status based on state', async () => {
    wrapper.vm.state = 'closed';
    await nextTick();

    expect(wrapper.vm.status).toStrictEqual('Disconnected');

    wrapper.vm.state = 'requested';
    await nextTick();

    expect(wrapper.vm.status).toStrictEqual('Waiting for host...');

    wrapper.vm.state = 'opened';
    await nextTick();

    expect(wrapper.vm.status).toStrictEqual('Connected');

    wrapper.vm.state = 'xyz';
    await nextTick();

    expect(wrapper.vm.status).toStrictEqual('Unknown');
  });

  it('should not send socket signal when host is simulated', () => {
    wrapper.unmount();

    wrapper = mount(Terminal, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
      },
      props: { id: 'abcd', host: { id: 'sim-xyz' }, idx: 0 },
    });

    onDataCallback('test data');

    expect(emit).not.toHaveBeenCalled();

    onResizeCallback({});

    expect(emit).not.toHaveBeenCalled();
  });

  it('should send data when available', () => {
    onDataCallback('test data');

    expect(emit).toHaveBeenCalledWith({
      event: 'terminal:data',
      type: 'console',
      data: 'test data',
      rayId: 'xyz:abcd',
    });
  });

  it('should send dimensions when resized', () => {
    onResizeCallback({ rows: 100, cols: 200 });

    expect(emit).toHaveBeenCalledWith({
      event: 'terminal:resize',
      type: 'console',
      data: { rows: 100, cols: 200 },
      rayId: 'xyz:abcd',
    });
  });

  it('should write data to terminal when received', async () => {
    expect(wrapper.vm.state).toStrictEqual('requested');

    bus.emit('xyz:abcd', 'test data');
    await nextTick();

    expect(wrapper.vm.state).toStrictEqual('opened');
    expect(write).toHaveBeenCalledWith('test data');
  });

  it('should write dummy data to terminal when host is simulated', async () => {
    wrapper.unmount();
    vi.useFakeTimers();

    wrapper = mount(Terminal, {
      global: {
        plugins: [mockStore, mockRouter],
        directives: { hide() {} },
      },
      props: { id: 'abcd', host: { id: 'sim-xyz' }, idx: 0 },
    });

    vi.advanceTimersByTime(1500);

    expect(wrapper.vm.state).toStrictEqual('opened');
    expect(write).toHaveBeenCalledWith('This is a Simulated Host >');
  });
});
