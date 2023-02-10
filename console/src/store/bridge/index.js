import { mutationKeys, themes } from '@/store/constants';

const state = () => ({
  connected: false,
});

const mutations = {
  [mutationKeys.SET_CONNECTED]: (state, connected) => {
    state.connected = connected;
  },
};

const actions = {
  async setTheme({ getters: { getThemeClass, systemTheme }, commit, dispatch }, newTheme) {
    if (!Object.values(themes).includes(newTheme)) {
      return;
    }

    const themeClass = getThemeClass(newTheme === themes.SYSTEM ? systemTheme : newTheme);
    document.documentElement.dataset.theme = themeClass;

    if (newTheme === themes.SYSTEM) {
      document.documentElement.className = themeClass;
      commit(mutationKeys.SET_SYSTEM_THEME, systemTheme);
    } else {
      if (document.documentElement.className === '') {
        document.documentElement.classList.add(themeClass);
      } else {
        document.documentElement.classList.replace(getThemeClass(), themeClass);
      }
    }

    commit(mutationKeys.SET_THEME, newTheme);
    await dispatch('updatePreferences');
  },
  updatePreferences({ getters: { serializableState } }) {
    const serialized = JSON.stringify(serializableState);
    localStorage.setItem('user-preferences', serialized);
  },
  async loadPreferences({ dispatch }) {
    const storedPreferences = JSON.parse(localStorage.getItem('user-preferences'));

    if (!storedPreferences) {
      await dispatch('setTheme', themes.SYSTEM);
    } else {
      storedPreferences.theme =
        Object.values(themes).find((theme) => theme === storedPreferences.theme) || themes.SYSTEM;

      await dispatch('setTheme', storedPreferences.theme);
    }
  },
};

const getters = {
  getThemeClass:
    (state, { systemTheme }) =>
    (theme) => {
      theme = theme ?? state.theme;
      return theme === themes.SYSTEM ? systemTheme : theme;
    },
  serializableState: (state) => {
    const serializableState = {};

    serializableState.theme = state.theme;

    return serializableState;
  },
  theme: (state, { systemTheme }) => (state.theme === themes.SYSTEM ? systemTheme : state.theme),
  systemTheme: () => {
    if (window.matchMedia('(prefers-contrast: more)').matches) {
      return themes.HIGH_CONTRAST;
    }
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return themes.DARK;
    }

    return themes.LIGHT;
  },
};

export default {
  state,
  mutations,
  actions,
  getters,
};
