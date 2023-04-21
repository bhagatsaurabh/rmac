import { describe, expect, it, vi } from 'vitest';
import store from '@/store';
import { mutationKeys, themes } from '../constants';

describe('Preferences Store Mutations', () => {
  it('should set theme', () => {
    store.state.preferences.theme = themes.SYSTEM;

    store.commit(mutationKeys.SET_THEME, themes.HIGH_CONTRAST);

    expect(store.state.preferences.theme).toStrictEqual(themes.HIGH_CONTRAST);
  });

  it('should set system theme', () => {
    store.state.preferences.sysTheme = themes.DARK;

    store.commit(mutationKeys.SET_SYSTEM_THEME, themes.LIGHT);

    expect(store.state.preferences.sysTheme).toStrictEqual(themes.LIGHT);
  });
});

describe('Preferences Store Actions', () => {
  it('should not set theme if provided theme is invalid', async () => {
    window.matchMedia = vi.fn(() => ({ matches: true }));
    store.state.preferences.theme = themes.LIGHT;

    await store.dispatch('setTheme', 'purple');

    expect(store.state.preferences.theme).toStrictEqual(themes.LIGHT);
  });

  it('should set theme if no theme is applied yet', async () => {
    window.matchMedia = vi.fn(() => ({ matches: true }));
    store.state.preferences.theme = themes.SYSTEM;
    document.documentElement.className = '';

    await store.dispatch('setTheme', themes.DARK);

    expect(store.state.preferences.theme).toStrictEqual(themes.DARK);
    expect(document.documentElement.classList.contains('dark')).toStrictEqual(true);
  });

  it('should set theme if previous theme is already applied', async () => {
    window.matchMedia = vi.fn(() => ({ matches: true }));
    store.state.preferences.theme = themes.SYSTEM;
    document.documentElement.className = 'high-contrast';

    await store.dispatch('setTheme', themes.LIGHT);

    expect(store.state.preferences.theme).toStrictEqual(themes.LIGHT);
    expect(document.documentElement.classList.contains('light')).toStrictEqual(true);
  });

  it('should set system theme', async () => {
    window.matchMedia = vi.fn(() => ({ matches: true }));
    store.state.preferences.theme = themes.LIGHT;
    document.documentElement.className = 'light';

    await store.dispatch('setTheme', themes.SYSTEM);

    expect(store.state.preferences.sysTheme).toStrictEqual(themes.HIGH_CONTRAST);
    expect(document.documentElement.classList.contains('high-contrast')).toStrictEqual(true);
  });

  it('should set system theme if preferences are not set already', async () => {
    window.localStorage = { getItem: () => 'null', setItem: vi.fn() };

    await store.dispatch('loadPreferences');

    expect(store.state.preferences.theme).toStrictEqual(themes.SYSTEM);
  });

  it('should set system theme if theme is invalid in preferences', async () => {
    window.localStorage = { getItem: () => JSON.stringify({ theme: 'xyz' }), setItem: vi.fn() };

    await store.dispatch('loadPreferences');

    expect(store.state.preferences.theme).toStrictEqual(themes.SYSTEM);
  });

  it('should set theme correctly if preferences are set', async () => {
    window.localStorage = { getItem: () => JSON.stringify({ theme: 'dark' }), setItem: vi.fn() };

    await store.dispatch('loadPreferences');

    expect(store.state.preferences.theme).toStrictEqual(themes.DARK);
  });
});

describe('Preferences Store Getters', () => {
  it('should get theme correctly', () => {
    window.matchMedia = vi.fn(() => ({ matches: true }));
    store.state.preferences.theme = themes.SYSTEM;

    expect(store.getters.theme).toStrictEqual(themes.HIGH_CONTRAST);

    store.state.preferences.theme = themes.DARK;

    expect(store.getters.theme).toStrictEqual(themes.DARK);
  });

  it('should get system theme correctly', () => {
    window.matchMedia = vi.fn().mockReturnValueOnce({ matches: true });

    expect(store.getters.systemTheme).toStrictEqual(themes.HIGH_CONTRAST);
  });
});
