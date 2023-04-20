import { describe, it, expect, vi, beforeEach } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import Property from '../Property/Property.vue';
import { nextTick } from 'vue';
import Button from '../Button/Button.vue';
import Toggle from '../Toggle/Toggle.vue';

describe('Property component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { updateProperty: vi.fn(() => false) };
    mockStore = createStore({
      modules: {},
      actions,
      state: { value: 1234 },
      mutations: { setValue: (state, data) => (state.value = data) },
      getters: {
        getHostById: (state) => (id) =>
          id === 'abc'
            ? { testField: state.value }
            : id === 'xyz'
            ? { config: { testField: 'TestFieldValue' } }
            : { config: { testField: false } },
      },
    });
  });

  it('should render', () => {
    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { id: 'abc', type: 'global', name: 'testField', inputType: 'number' },
      slots: {
        default: 'Test Field',
      },
    });

    expect(wrapper.vm).toBeDefined();
    expect(wrapper.find('div.name').text()).toStrictEqual('Test Field');
  });

  it('should not edit when property is non-editable', async () => {
    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { id: 'abc', type: 'global', name: 'testField', inputType: 'number' },
    });

    await wrapper.find('span.input').trigger('click');

    expect(wrapper.vm.state).toStrictEqual('idle');
  });

  it('should restore value to original value when property update fails', async () => {
    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { id: 'abc', type: 'global', name: 'testField', inputType: 'number', editable: true },
      attachTo: document.body,
    });

    await wrapper.find('span.input').trigger('click');
    await wrapper.find('input').setValue(5678);

    wrapper.find('span.controls').findComponent(Button).vm.$emit('click');
    await flushPromises();

    expect(actions.updateProperty.mock.calls[0][1]).toStrictEqual({
      id: 'abc',
      prop: {
        name: 'TestField',
        value: 5678,
      },
    });
    expect(wrapper.vm.value).toStrictEqual(1234);
    expect(wrapper.vm.state).toStrictEqual('idle');
  });

  it("should change state to 'syncing' value when property update succeeds", async () => {
    actions = { updateProperty: vi.fn(() => true) };
    mockStore = createStore({
      modules: {},
      actions,
      getters: {
        getHostById: () => (id) =>
          id === 'abc' ? { testField: 1234 } : { config: { testField: 'TestFieldValue' } },
      },
    });
    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { id: 'abc', type: 'global', name: 'testField', inputType: 'number', editable: true },
      attachTo: document.body,
    });

    await wrapper.find('span.input').trigger('click');
    await wrapper.find('input').setValue(5678);

    wrapper.find('span.controls').findComponent(Button).vm.$emit('click');
    await flushPromises();

    expect(actions.updateProperty.mock.calls[0][1]).toStrictEqual({
      id: 'abc',
      prop: {
        name: 'TestField',
        value: 5678,
      },
    });
    expect(wrapper.vm.value).toStrictEqual(5678);
    expect(wrapper.vm.state).toStrictEqual('idle');
    expect(wrapper.vm.syncing).toStrictEqual(true);
  });

  it("should change state to 'idle' when input value is not modified and loses focus", async () => {
    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { id: 'abc', type: 'global', name: 'testField', inputType: 'number', editable: true },
      attachTo: document.body,
    });

    await wrapper.find('span.input').trigger('click');

    expect(wrapper.vm.state).toStrictEqual('editing');

    await wrapper.find('input').trigger('blur');

    expect(wrapper.vm.state).toStrictEqual('idle');
  });

  it("should change state to 'edited' when input value has been modified and loses focus", async () => {
    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { id: 'abc', type: 'global', name: 'testField', inputType: 'number', editable: true },
      attachTo: document.body,
    });

    await wrapper.find('span.input').trigger('click');

    expect(wrapper.vm.state).toStrictEqual('editing');

    await wrapper.find('input').setValue(5678);
    await wrapper.find('input').trigger('blur');

    expect(wrapper.vm.state).toStrictEqual('edited');
  });

  it('should copy field value to clipboard', async () => {
    vi.useFakeTimers();

    const writeText = vi.fn();
    window.navigator = { clipboard: { writeText } };

    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { id: 'abc', type: 'global', name: 'testField', inputType: 'number', editable: true },
      attachTo: document.body,
    });

    wrapper.find('button.copy').trigger('click');
    await nextTick();

    expect(writeText).toHaveBeenCalledWith(1234);
    expect(wrapper.vm.copied).toStrictEqual(true);

    vi.advanceTimersByTime(3000);

    expect(wrapper.vm.copied).toStrictEqual(false);
  });

  it("should reset 'syncing' state when original value changes to be same as modified value", async () => {
    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: { id: 'abc', type: 'global', name: 'testField', inputType: 'number', editable: true },
      attachTo: document.body,
    });

    wrapper.vm.syncing = true;
    wrapper.vm.value = 5678;

    mockStore.commit('setValue', 5678);
    await nextTick();

    expect(wrapper.vm.syncing).toStrictEqual(false);

    wrapper.unmount();
  });

  it("should render when inputType is 'password'", () => {
    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: {
        id: 'xyz',
        type: 'config',
        name: 'testField',
        inputType: 'password',
        editable: true,
      },
      attachTo: document.body,
    });

    expect(wrapper.vm).toBeDefined();
  });

  it("should render when inputType is 'checkbox'", async () => {
    wrapper = mount(Property, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
      props: {
        id: 'lmn',
        type: 'config',
        name: 'testField',
        inputType: 'checkbox',
        editable: true,
      },
      attachTo: document.body,
    });

    expect(wrapper.vm).toBeDefined();

    await wrapper.findComponent(Toggle).setValue(false);

    expect(wrapper.vm.value).toStrictEqual(false);
  });
});
