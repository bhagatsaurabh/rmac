export const themes = Object.freeze({
  SYSTEM: Symbol('system'),
  LIGHT: Symbol('light'),
  DARK: Symbol('dark'),
  HIGH_CONTRAST: Symbol('high-contrast'),
});

export const mutationKeys = Object.freeze({
  SET_THEME: 'SET_THEME',
  SET_PREFERENCES: 'SET_PREFERENCES',
  SET_SYSTEM_THEME: 'SET_SYSTEM_THEME',
});
