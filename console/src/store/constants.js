export const themes = Object.freeze({
  SYSTEM: Symbol('system'),
  LIGHT: Symbol('light'),
  DARK: Symbol('dark'),
  HIGH_CONTRAST: Symbol('high-contrast'),
});

export const mutationKeys = Object.freeze({
  SET_THEME: Symbol('SET_THEME'),
  SET_PREFERENCES: Symbol('SET_PREFERENCES'),
  SET_SYSTEM_THEME: Symbol('SET_SYSTEM_THEME'),
});
