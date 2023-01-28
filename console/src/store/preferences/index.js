import { mutationKeys, themes } from '@/store/constants';

const state = () => ({
  theme: themes.SYSTEM,
  systemTheme: themes.LIGHT,
});

const mutations = {
  [mutationKeys.SET_THEME]: (state, theme) => {
    state.theme = theme;
  },
  [mutationKeys.SET_SYSTEM_THEME]: (state, theme) => {
    state.systemTheme = theme;
  },
  [mutationKeys.SET_PREFERENCES]: (state, preferences) => {
    state.theme = preferences.theme;
    state.systemTheme = preferences.systemTheme;
  },
};

const actions = {
  setTheme({ getters, state, commit, dispatch }, newTheme) {
    const oldTheme = state.theme;

    if (typeof newTheme !== 'symbol' || !Object.values(themes).includes(newTheme)) {
      return;
    }

    if (newTheme === themes.SYSTEM) {
      document.documentElement.className = '';
    } else if (oldTheme === themes.SYSTEM) {
      document.documentElement.classList.add(getters.getClassName(newTheme));
    } else {
      document.documentElement.classList.replace(
        getters.getClassName(),
        getters.getClassName(newTheme)
      );
    }

    commit(mutationKeys.SET_THEME, newTheme);
    dispatch('updatePreferences');
  },
  setSystemTheme({ commit }, theme) {
    if (typeof theme !== 'symbol' || !Object.values(themes).includes(theme)) {
      return;
    }

    commit(mutationKeys.SET_SYSTEM_THEME, theme);
  },
  updatePreferences({ getters }) {
    const serialized = JSON.stringify(getters.getSerializableState());
    localStorage.setItem('user-preferences', serialized);
  },
  loadPreferences({ getters, commit }) {
    const storedPreferences = JSON.parse(localStorage.getItem('user-preferences'));

    if (!storedPreferences) {
      commit(mutationKeys.SET_SYSTEM_THEME, getters.getSystemTheme());
    } else {
      storedPreferences.theme = Object.values(themes).find(
        (theme) => theme.description === storedPreferences.theme
      );
      storedPreferences.systemTheme = getters.getSystemTheme();

      commit(mutationKeys.SET_PREFERENCES, storedPreferences);
    }
  },
};

const getters = {
  getClassName: (state) => (theme) => theme?.description ?? state.theme.description,
  getSerializableState: (state) => {
    const serializableState = {};

    serializableState.theme = state.theme.description;

    return serializableState;
  },
  getTheme: (state) => {
    if (state.theme === themes.SYSTEM) return state.systemTheme;
    return state.theme;
  },
  getSystemTheme: () => () => {
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
