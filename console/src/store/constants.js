const themes = Object.freeze({
  SYSTEM: 'system',
  LIGHT: 'light',
  DARK: 'dark',
  HIGH_CONTRAST: 'high-contrast',
});

const themeNames = {
  system: 'System',
  light: 'Light',
  dark: 'Dark',
  'high-contrast': 'High Contrast',
};

const themeName = (theme) => themeNames[theme];

const mutationKeys = Object.freeze({
  SET_THEME: 'SET_THEME',
  SET_SYSTEM_THEME: 'SET_SYSTEM_THEME',
});

export { themes, themeName, mutationKeys };
