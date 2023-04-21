import { describe, expect, it, vi } from 'vitest';
import store from '@/store';
import { mutationKeys, notifications } from '../constants';
import simulatedHosts from '@/assets/simulated-hosts.json';
import bus from '@/event';
import * as skt from '@/socket';

vi.mock('@/event', () => ({
  default: {
    emit: vi.fn(),
  },
}));
vi.mock('@/socket', () => ({
  emit: vi.fn(),
  onMessage: vi.fn(),
}));

describe('Hosts Store Mutations', () => {
  it('should set host health', () => {
    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'lmn' }, { id: 'xyz' }];

    store.commit(mutationKeys.SET_HOST_HEALTH, { id: 'lmn', health: true });

    expect(store.state.hosts.hosts[1].health).toStrictEqual(true);
  });

  it('should set hosts to blank array when data received is non-existent', () => {
    store.commit(mutationKeys.SET_HOSTS, null);

    expect(store.state.hosts.hosts).toStrictEqual([]);
  });

  it('should set hosts correctly', () => {
    store.commit(mutationKeys.SET_HOSTS, [
      { id: '123' },
      { id: '456' },
      { id: '789' },
      { id: '012' },
    ]);

    expect(store.state.hosts.hosts).toStrictEqual([
      { id: '123' },
      { id: '456' },
      { id: '789' },
      { id: '012' },
    ]);
  });

  it('should set filtered hosts to blank array when data received is non-existent', () => {
    store.commit(mutationKeys.SET_FILTERED_HOSTS, null);

    expect(store.state.hosts.filteredHosts).toStrictEqual([]);
  });

  it('should set filtered hosts correctly', () => {
    store.commit(mutationKeys.SET_FILTERED_HOSTS, [
      { id: '123' },
      { id: '456' },
      { id: '789' },
      { id: '012' },
    ]);

    expect(store.state.hosts.filteredHosts).toStrictEqual([
      { id: '123' },
      { id: '456' },
      { id: '789' },
      { id: '012' },
    ]);
  });

  it('should set host config', () => {
    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'lmn' }, { id: 'xyz' }];

    store.commit(mutationKeys.SET_HOST_CONFIG, {
      hId: 'lmn',
      data: { clientName: 'Test client name', hostName: 'Test host name', abcd: '1234' },
    });

    expect(store.state.hosts.hosts[1]).toStrictEqual({
      id: 'lmn',
      config: { clientName: 'Test client name', hostName: 'Test host name', abcd: '1234' },
      clientName: 'Test client name',
      hostName: 'Test host name',
    });
  });

  it('should set host id', () => {
    store.state.hosts.hosts = [{ id: '123' }, { id: '456' }, { id: '789' }];

    store.commit(mutationKeys.SET_HOST_ID, { oldId: '789', newId: 'abcd' });

    expect(store.state.hosts.hosts[2].id).toStrictEqual('abcd');
    expect(store.state.hosts.hosts[2].registered).toStrictEqual(true);
  });

  it('should add simulated hosts', () => {
    store.state.hosts.hosts = [];

    store.commit(mutationKeys.ADD_SIMULATED_HOSTS);

    expect(store.state.hosts.hosts).toStrictEqual(simulatedHosts);
  });
});

describe('Hosts Store Actions', () => {
  it('should notify when fetchHosts fails', async () => {
    window.fetch = vi.fn().mockRejectedValue(new Error('Fetching hosts failed xyz'));

    const result = await store.dispatch('fetchHosts');

    expect(result).toBeFalsy();
    expect(bus.emit).toHaveBeenCalledWith('notify', {
      ...notifications.EFETCH_HOSTS_FAILED(),
      desc: 'Fetching hosts failed xyz',
    });
  });

  it('should fetch hosts', async () => {
    window.fetch = vi
      .fn()
      .mockResolvedValue({ json: () => [{ id: 'abc' }, { id: 'def' }, { id: 'ghi' }] });

    const result = await store.dispatch('fetchHosts');

    expect(result).toBeTruthy();
    expect(store.state.hosts.hosts).toStrictEqual([
      { id: 'abc' },
      { id: 'def' },
      { id: 'ghi' },
      ...simulatedHosts,
    ]);
  });

  it('should filter hosts', async () => {
    store.state.hosts.hosts = [
      {
        id: 'abc',
        clientName: 'qopeorutn',
        health: true,
        registration: false,
      },
      {
        id: 'def',
        clientName: 'nrthttty',
        health: false,
        registration: false,
      },
      {
        id: 'sim-ghi',
        clientName: 'kuounubty',
        health: false,
        registration: true,
      },
      {
        id: 'jkl',
        clientName: 'acdcefef',
        health: false,
        registration: true,
      },
      {
        id: 'sim-mno',
        clientName: 'palwepire',
        health: true,
        registration: false,
      },
    ];

    await store.dispatch('filter', {
      config: {
        name: 'p',
        filter: {
          connection: ['online'],
          registration: ['unknown'],
        },
        sort: {},
        simulated: true,
      },
      hosts: store.state.hosts.hosts,
    });

    expect(store.state.hosts.filteredHosts).toStrictEqual([
      {
        id: 'abc',
        clientName: 'qopeorutn',
        health: true,
        registration: false,
      },
      {
        id: 'sim-mno',
        clientName: 'palwepire',
        health: true,
        registration: false,
      },
    ]);

    await store.dispatch('filter', {
      config: {
        name: '',
        filter: {
          connection: ['offline'],
          registration: ['registered'],
        },
        sort: {},
        simulated: true,
      },
      hosts: store.state.hosts.hosts,
    });

    expect(store.state.hosts.filteredHosts).toStrictEqual([
      {
        id: 'sim-ghi',
        clientName: 'kuounubty',
        health: false,
        registration: true,
      },
      {
        id: 'jkl',
        clientName: 'acdcefef',
        health: false,
        registration: true,
      },
    ]);
  });

  it('should sort hosts', async () => {
    store.state.hosts.hosts = [
      {
        id: 'abc',
        clientName: 'qopeorutn',
        health: true,
        registration: false,
      },
      {
        id: 'def',
        clientName: 'nrthttty',
        health: false,
        registration: false,
      },
      {
        id: 'sim-ghi',
        clientName: 'kuounubty',
        health: false,
        registration: true,
      },
      {
        id: 'jkl',
        clientName: 'acdcefef',
        health: false,
        registration: true,
      },
      {
        id: 'sim-mno',
        clientName: 'palwepire',
        health: true,
        registration: false,
      },
    ];

    await store.dispatch('filter', {
      config: {
        name: '',
        filter: {
          connection: [],
          registration: [],
        },
        sort: {
          type: 'name',
          order: false,
        },
        simulated: true,
      },
      hosts: store.state.hosts.hosts,
    });

    expect(store.state.hosts.filteredHosts).toStrictEqual([
      {
        id: 'abc',
        clientName: 'qopeorutn',
        health: true,
        registration: false,
      },
      {
        id: 'sim-mno',
        clientName: 'palwepire',
        health: true,
        registration: false,
      },
      {
        id: 'def',
        clientName: 'nrthttty',
        health: false,
        registration: false,
      },
      {
        id: 'sim-ghi',
        clientName: 'kuounubty',
        health: false,
        registration: true,
      },
      {
        id: 'jkl',
        clientName: 'acdcefef',
        health: false,
        registration: true,
      },
    ]);

    await store.dispatch('filter', {
      config: {
        name: '',
        filter: {
          connection: [],
          registration: [],
        },
        sort: {
          type: 'name',
          order: true,
        },
        simulated: true,
      },
      hosts: store.state.hosts.hosts,
    });

    expect(store.state.hosts.filteredHosts).toStrictEqual([
      {
        id: 'jkl',
        clientName: 'acdcefef',
        health: false,
        registration: true,
      },
      {
        id: 'sim-ghi',
        clientName: 'kuounubty',
        health: false,
        registration: true,
      },

      {
        id: 'def',
        clientName: 'nrthttty',
        health: false,
        registration: false,
      },
      {
        id: 'sim-mno',
        clientName: 'palwepire',
        health: true,
        registration: false,
      },
      {
        id: 'abc',
        clientName: 'qopeorutn',
        health: true,
        registration: false,
      },
    ]);

    await store.dispatch('filter', {
      config: {
        name: '',
        filter: {
          connection: [],
          registration: [],
        },
        sort: {
          type: 'connection',
          order: false,
        },
        simulated: true,
      },
      hosts: store.state.hosts.hosts,
    });

    expect(store.state.hosts.filteredHosts).toStrictEqual([
      {
        id: 'abc',
        clientName: 'qopeorutn',
        health: true,
        registration: false,
      },
      {
        id: 'sim-mno',
        clientName: 'palwepire',
        health: true,
        registration: false,
      },
      {
        id: 'def',
        clientName: 'nrthttty',
        health: false,
        registration: false,
      },
      {
        id: 'sim-ghi',
        clientName: 'kuounubty',
        health: false,
        registration: true,
      },
      {
        id: 'jkl',
        clientName: 'acdcefef',
        health: false,
        registration: true,
      },
    ]);

    await store.dispatch('filter', {
      config: {
        name: '',
        filter: {
          connection: [],
          registration: [],
        },
        sort: {
          type: 'connection',
          order: true,
        },
        simulated: true,
      },
      hosts: store.state.hosts.hosts,
    });

    expect(store.state.hosts.filteredHosts).toStrictEqual([
      {
        id: 'def',
        clientName: 'nrthttty',
        health: false,
        registration: false,
      },
      {
        id: 'sim-ghi',
        clientName: 'kuounubty',
        health: false,
        registration: true,
      },
      {
        id: 'jkl',
        clientName: 'acdcefef',
        health: false,
        registration: true,
      },
      {
        id: 'abc',
        clientName: 'qopeorutn',
        health: true,
        registration: false,
      },
      {
        id: 'sim-mno',
        clientName: 'palwepire',
        health: true,
        registration: false,
      },
    ]);

    await store.dispatch('filter', {
      config: {
        name: '',
        filter: {
          connection: [],
          registration: [],
        },
        sort: {
          type: 'registration',
          order: false,
        },
        simulated: true,
      },
      hosts: store.state.hosts.hosts,
    });

    expect(store.state.hosts.filteredHosts).toStrictEqual([
      {
        id: 'abc',
        clientName: 'qopeorutn',
        health: true,
        registration: false,
      },
      {
        id: 'def',
        clientName: 'nrthttty',
        health: false,
        registration: false,
      },
      {
        id: 'sim-ghi',
        clientName: 'kuounubty',
        health: false,
        registration: true,
      },
      {
        id: 'jkl',
        clientName: 'acdcefef',
        health: false,
        registration: true,
      },
      {
        id: 'sim-mno',
        clientName: 'palwepire',
        health: true,
        registration: false,
      },
    ]);

    await store.dispatch('filter', {
      config: {
        name: '',
        filter: {
          connection: [],
          registration: [],
        },
        sort: {
          type: 'registration',
          order: true,
        },
        simulated: true,
      },
      hosts: store.state.hosts.hosts,
    });

    expect(store.state.hosts.filteredHosts).toStrictEqual([
      {
        id: 'abc',
        clientName: 'qopeorutn',
        health: true,
        registration: false,
      },
      {
        id: 'def',
        clientName: 'nrthttty',
        health: false,
        registration: false,
      },
      {
        id: 'sim-ghi',
        clientName: 'kuounubty',
        health: false,
        registration: true,
      },
      {
        id: 'jkl',
        clientName: 'acdcefef',
        health: false,
        registration: true,
      },
      {
        id: 'sim-mno',
        clientName: 'palwepire',
        health: true,
        registration: false,
      },
    ]);
  });

  it('should fake fetchConfig when host is simulated', async () => {
    window.fetch = vi.fn();
    vi.useFakeTimers();
    vi.advanceTimersByTimeAsync(1500);

    const result = await store.dispatch('fetchConfig', 'sim-1234');

    expect(window.fetch).not.toHaveBeenCalled();
    expect(result).toBeTruthy();

    vi.useRealTimers();
  });

  it('should notify when fetchConfig fails', async () => {
    vi.resetAllMocks();
    window.fetch = vi.fn().mockRejectedValue(new Error('Fetching host config failed qwerty'));

    const result = await store.dispatch('fetchConfig', '1234');

    expect(result).toBeFalsy();
    expect(bus.emit).toHaveBeenCalledWith('notify', {
      ...notifications.EFETCH_HOST_CONFIG_FAILED(),
      desc: 'Fetching host config failed qwerty',
    });
  });

  it('should fetchConfig successfully', async () => {
    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'def' }, { id: 'ghi' }, { id: 'jkl' }];
    window.fetch = vi.fn().mockResolvedValue({ json: () => ({ xyz: 'abc' }), status: 200 });

    const result = await store.dispatch('fetchConfig', 'ghi');

    expect(result).toBeTruthy();
    expect(store.state.hosts.hosts[2].config).toStrictEqual({ xyz: 'abc' });
  });

  it('should notify if host if offline while updating property', async () => {
    vi.resetAllMocks();
    store.state.hosts.hosts = [
      { id: 'abc' },
      { id: 'xyz', health: false, clientName: 'TestClient' },
      { id: 'lmn' },
    ];

    const result = await store.dispatch('updateProperty', {
      id: 'xyz',
      prop: {
        name: 'field1',
        value: 'value1',
      },
    });

    expect(result).toBeFalsy();
    expect(bus.emit).toHaveBeenCalledWith('notify', notifications.WHOST_OFFLINE('TestClient'));
  });

  it('should fake updateProperty when host is simulated', async () => {
    vi.resetAllMocks();
    vi.useFakeTimers();

    store.state.hosts.hosts = [
      { id: 'abc' },
      { id: 'sim-xyz', health: true, config: { field1: 'old-value' } },
      { id: 'lmn' },
    ];

    vi.advanceTimersByTimeAsync(3200);
    const result = await store.dispatch('updateProperty', {
      id: 'sim-xyz',
      prop: {
        name: 'field1',
        value: 'new-value',
      },
    });

    expect(result).toBeTruthy();

    vi.useRealTimers();
  });

  it('should notify when updateProperty fails', async () => {
    vi.resetAllMocks();

    store.state.hosts.hosts = [
      { id: 'abc' },
      { id: 'def', health: true, config: { field: 'old-val' }, clientName: 'TestClient' },
      { id: 'ghi' },
    ];
    window.fetch = vi.fn().mockRejectedValue(new Error('Host property update failed zqxwce'));

    const result = await store.dispatch('updateProperty', {
      id: 'def',
      prop: { name: 'field', value: 'new-val' },
    });

    expect(result).toBeFalsy();
    expect(bus.emit).toHaveBeenCalledWith(
      'notify',
      notifications.EUPDATE_HOST_PROP_FAILED('TestClient', 'field', 'new-val')
    );
  });

  it('should updateProperty successfully', async () => {
    vi.resetAllMocks();

    store.state.hosts.hosts = [
      { id: 'abc' },
      { id: 'def', health: true, config: { field: 'old-val' }, clientName: 'TestClient' },
      { id: 'ghi' },
    ];
    window.fetch = vi.fn().mockResolvedValue();

    const result = await store.dispatch('updateProperty', {
      id: 'def',
      prop: { name: 'field', value: 'new-val' },
    });

    expect(result).toBeTruthy();
  });

  it('should ignore openTerminal if host is simulated', async () => {
    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'sim-def' }, { id: 'ghi' }];

    await store.dispatch('openTerminal', { hostId: 'sim-def', terminalId: '12345678' });

    expect(skt.emit).not.toHaveBeenCalled();
  });

  it('should openTerminal', async () => {
    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'def' }, { id: 'ghi' }];

    await store.dispatch('openTerminal', { hostId: 'def', terminalId: '12345678' });

    expect(skt.emit).toHaveBeenCalledWith({
      event: 'terminal:open',
      type: 'console',
      data: null,
      rayId: 'def:12345678',
    });
  });

  it('should ignore closeTerminal if host is simulated', async () => {
    vi.resetAllMocks();

    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'sim-def' }, { id: 'ghi' }];

    await store.dispatch('closeTerminal', { hostId: 'sim-def', terminalId: '12345678' });

    expect(skt.emit).not.toHaveBeenCalled();
  });

  it('should closeTerminal', async () => {
    vi.resetAllMocks();

    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'def' }, { id: 'ghi' }];

    await store.dispatch('closeTerminal', { hostId: 'def', terminalId: '12345678' });

    expect(skt.emit).toHaveBeenCalledWith({
      event: 'terminal:close',
      type: 'console',
      data: null,
      rayId: 'def:12345678',
    });
  });

  it('should fake sendCommand when host is simulated', async () => {
    vi.useFakeTimers();
    vi.resetAllMocks();

    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'sim-def' }, { id: 'ghi' }];

    vi.advanceTimersByTimeAsync(2000);
    await store.dispatch('sendCommand', { hostId: 'sim-def', command: 'test command one' });

    expect(bus.emit).toHaveBeenCalledWith('notify', notifications.ISEND_COMMAND_SUCCESS());

    vi.useRealTimers();
  });

  it('should notify when sendCommand fails if host is offline', async () => {
    vi.resetAllMocks();

    window.fetch = vi.fn().mockResolvedValue({
      status: 400,
      json: () => ({ message: 'Command send failed qpwoeiru' }),
    });

    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'def', health: false }, { id: 'ghi' }];

    await store.dispatch('sendCommand', { hostId: 'def', command: 'test command one' });

    expect(bus.emit).toHaveBeenCalledWith(
      'notify',
      notifications.ESEND_COMMAND_FAILED({ message: 'Command send failed qpwoeiru' })
    );
  });

  it('should notify when sendCommand succeeds if host is offline', async () => {
    vi.resetAllMocks();

    window.fetch = vi.fn().mockResolvedValue({
      status: 200,
    });

    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'def', health: false }, { id: 'ghi' }];

    await store.dispatch('sendCommand', { hostId: 'def', command: 'test command one' });

    expect(bus.emit).toHaveBeenCalledWith('notify', notifications.ISEND_COMMAND_SUCCESS());
  });

  it('should sendCommand if host is online', async () => {
    vi.resetAllMocks();

    store.state.hosts.hosts = [{ id: 'abc' }, { id: 'def', health: true }, { id: 'ghi' }];

    await store.dispatch('sendCommand', { hostId: 'def', command: 'test command one' });

    expect(skt.emit).toHaveBeenCalledWith({
      event: 'command',
      type: 'console',
      data: 'test command one',
      rayId: 'def',
    });
  });
});
