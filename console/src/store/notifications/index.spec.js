import { describe, expect, it } from 'vitest';
import store from '@/store';
import { mutationKeys, notifications } from '../constants';

describe('Notifications Store Mutations', () => {
  it('should push notification', () => {
    store.state.notifications.data = [];

    store.commit(mutationKeys.PUSH_NOTIFICATION, notifications.IHOST_ONLINE());

    expect(store.state.notifications.data[0]).toStrictEqual({
      ...notifications.IHOST_ONLINE(),
      read: false,
    });
  });

  it('should mark all notifications as read', () => {
    store.state.notifications.data = [
      { read: false },
      { read: false },
      { read: false },
      { read: false },
    ];

    store.commit(mutationKeys.SET_READ_ALL);

    expect(store.state.notifications.data).toStrictEqual([
      { read: true },
      { read: true },
      { read: true },
      { read: true },
    ]);
  });
});

describe('Notifications Store Actions', () => {
  it('should push notification', async () => {
    store.state.notifications.data = [];

    await store.dispatch(
      'pushNotification',
      notifications.EUPDATE_HOST_PROP_FAILED('TestClient', 'field', 'val')
    );

    expect(store.state.notifications.data).toStrictEqual([
      { ...notifications.EUPDATE_HOST_PROP_FAILED('TestClient', 'field', 'val'), read: false },
    ]);
  });

  it('should read all notifications', async () => {
    store.state.notifications.data = [
      { read: false },
      { read: false },
      { read: false },
      { read: false },
    ];

    await store.dispatch('readAllNotifications');

    expect(store.state.notifications.data).toStrictEqual([
      { read: true },
      { read: true },
      { read: true },
      { read: true },
    ]);
  });
});
