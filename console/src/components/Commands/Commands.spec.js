import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Commands from '../Commands/Commands.vue';

describe('Commands component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { sendCommand: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
    });

    wrapper = mount(Commands, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { host: { id: '12345678' } },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it("should dispatch correct action when 'shutdown' button is clicked", async () => {
    await wrapper.findAll('button').at(1).trigger('click');

    expect(actions.sendCommand.mock.calls[0][1]).toStrictEqual({
      hostId: '12345678',
      command: 'system shutdown',
    });
    expect(wrapper.find('input').element.value).toBe('');
  });

  it('should dispatch correct action when custom command is entered and submitted', async () => {
    await wrapper.find('input').setValue('test command');
    await wrapper.find('form').trigger('submit');

    expect(actions.sendCommand.mock.calls[0][1]).toStrictEqual({
      hostId: '12345678',
      command: 'test command',
    });
    expect(wrapper.find('input').element.value).toBe('');
  });

  it("should dispatch correct action when custom command is entered and 'Send' button is clicked", async () => {
    await wrapper.find('input').setValue('test command two');
    await wrapper.findAll('button').at(2).trigger('click');

    expect(actions.sendCommand.mock.calls[0][1]).toStrictEqual({
      hostId: '12345678',
      command: 'test command two',
    });
    expect(wrapper.find('input').element.value).toBe('');
  });
});
