import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createStore } from 'vuex';
import SearchFilter from '../SearchFilter/SearchFilter.vue';
import { nextTick } from 'vue';
import Backdrop from '../Backdrop/Backdrop.vue';
import Toggle from '../Toggle/Toggle.vue';

describe('SearchFilter component', () => {
  let wrapper, actions, mockStore;
  beforeEach(() => {
    actions = { fetchHosts: vi.fn(), disconnectFromBridge: vi.fn() };
    mockStore = createStore({
      modules: {},
      actions,
    });

    wrapper = mount(SearchFilter, {
      global: {
        plugins: [mockStore],
        directives: { hide() {} },
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should emit query when config changes', async () => {
    wrapper.vm.changeHandler();
    await nextTick();

    expect(wrapper.emitted().query[0]).toStrictEqual([
      {
        name: '',
        filter: { connection: [], registration: [] },
        sort: { type: 'name', order: true },
        simulated: true,
      },
    ]);
  });

  it('should emit correct query when sort option changes', async () => {
    await wrapper.find('div.options').findAll('.options-category > button').at(0).trigger('click');

    expect(wrapper.emitted().query[0]).toStrictEqual([
      {
        name: '',
        filter: { connection: [], registration: [] },
        sort: { type: 'name', order: false },
        simulated: true,
      },
    ]);

    await wrapper.find('div.options').findAll('.options-category > button').at(1).trigger('click');

    expect(wrapper.emitted().query[0]).toStrictEqual([
      {
        name: '',
        filter: { connection: [], registration: [] },
        sort: { type: 'connection', order: false },
        simulated: true,
      },
    ]);

    await wrapper.find('div.options').findAll('.options-category > button').at(2).trigger('click');

    expect(wrapper.emitted().query[0]).toStrictEqual([
      {
        name: '',
        filter: { connection: [], registration: [] },
        sort: { type: 'registration', order: false },
        simulated: true,
      },
    ]);
  });

  it('should emit correct query when clearing sort setting', async () => {
    await wrapper.find('div.options').find('.options-category button').trigger('click');

    expect(wrapper.emitted().query[0]).toStrictEqual([
      {
        name: '',
        filter: { connection: [], registration: [] },
        sort: { type: null, order: true },
        simulated: true,
      },
    ]);
  });

  it('should emit correct query when clearing filter setting', async () => {
    await wrapper
      .find('div.options')
      .findAll('.options-category')
      .at(1)
      .find('button')
      .trigger('click');

    expect(wrapper.emitted().query[0]).toStrictEqual([
      {
        name: '',
        filter: { connection: [], registration: [] },
        sort: { type: 'name', order: true },
        simulated: true,
      },
    ]);
  });

  it('should fetch hosts from server and emit query change when refreshed', async () => {
    await wrapper.find('div.controls').findAll('button').at(1).trigger('click');

    expect(actions.fetchHosts).toHaveBeenCalled();
    expect(wrapper.emitted().query[0]).toStrictEqual([
      {
        name: '',
        filter: { connection: [], registration: [] },
        sort: { type: 'name', order: true },
        simulated: true,
      },
    ]);
  });

  it('should dispatch correct action when disconnecting', async () => {
    await wrapper.find('div.controls').findAll('button').at(0).trigger('click');

    expect(actions.disconnectFromBridge).toHaveBeenCalled();
  });

  it('should open search & filter menu when button is pressed', async () => {
    await wrapper.find('div.controls').findAll('button').at(2).trigger('click');

    expect(wrapper.vm.open).toStrictEqual(true);
    expect(wrapper.find('div.options').classes().includes('open')).toStrictEqual(true);
  });

  it('should close search & filter menu when backdrop is dismissed', async () => {
    await wrapper.find('div.controls').findAll('button').at(2).trigger('click');

    expect(wrapper.vm.open).toStrictEqual(true);
    expect(wrapper.find('div.options').classes().includes('open')).toStrictEqual(true);

    wrapper.findComponent(Backdrop).vm.$emit('dismiss');
    await nextTick();

    expect(wrapper.vm.open).toStrictEqual(false);
    expect(wrapper.find('div.options').classes().includes('open')).toStrictEqual(false);
  });

  it('should emit correct query when search & filter setting changes', async () => {
    await wrapper.find('input.search-input').setValue('searched name');

    expect(wrapper.emitted().query[0]).toStrictEqual([
      {
        name: 'searched name',
        filter: { connection: [], registration: [] },
        sort: { type: 'name', order: true },
        simulated: true,
      },
    ]);

    await wrapper.findComponent(Toggle).setValue(false);

    expect(wrapper.emitted().query[1]).toStrictEqual([
      {
        name: 'searched name',
        filter: { connection: [], registration: [] },
        sort: { type: 'name', order: true },
        simulated: false,
      },
    ]);

    await wrapper
      .findAll('.options .options-category')
      .at(1)
      .find('input#filter-connection-online')
      .setValue(true);

    expect(wrapper.emitted().query[2]).toStrictEqual([
      {
        name: 'searched name',
        filter: { connection: ['online'], registration: [] },
        sort: { type: 'name', order: true },
        simulated: false,
      },
    ]);

    await wrapper
      .findAll('.options .options-category')
      .at(1)
      .find('input#filter-connection-offline')
      .setValue(true);

    expect(wrapper.emitted().query[3]).toStrictEqual([
      {
        name: 'searched name',
        filter: { connection: ['online', 'offline'], registration: [] },
        sort: { type: 'name', order: true },
        simulated: false,
      },
    ]);

    await wrapper
      .findAll('.options .options-category')
      .at(1)
      .find('input#filter-registration-registered')
      .setValue(true);

    expect(wrapper.emitted().query[4]).toStrictEqual([
      {
        name: 'searched name',
        filter: { connection: ['online', 'offline'], registration: ['registered'] },
        sort: { type: 'name', order: true },
        simulated: false,
      },
    ]);

    await wrapper
      .findAll('.options .options-category')
      .at(1)
      .find('input#filter-registration-unknown')
      .setValue(true);

    expect(wrapper.emitted().query[5]).toStrictEqual([
      {
        name: 'searched name',
        filter: { connection: ['online', 'offline'], registration: ['registered', 'unknown'] },
        sort: { type: 'name', order: true },
        simulated: false,
      },
    ]);
  });
});
