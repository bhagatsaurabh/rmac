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
  SET_CONNECTED: 'SET_CONNECTED',
  SET_PING_TIMER: 'SET_PING_TIMER',
  SET_STATUS_MSG: 'SET_STATUS_MSG',
});

const notificationTypes = Object.freeze({
  SUCCESS: 'SUCCESS',
  INFO: 'INFO',
  WARN: 'WARN',
  ERROR: 'ERROR',
});

const notifications = Object.freeze({
  ECONN_FAILED: {
    type: notificationTypes.ERROR,
    title: 'Connection Failed',
    desc: 'Connection to bridge server failed',
  },
});

export { themes, themeName, mutationKeys, notificationTypes, notifications };
