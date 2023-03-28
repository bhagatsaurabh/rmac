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

let apiURL = import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL;
if (apiURL) {
  apiURL = apiURL.replace('ws', 'http');
}
apiURL = `${apiURL || ''}/api`;

const themeName = (theme) => themeNames[theme];

const mutationKeys = Object.freeze({
  SET_THEME: 'SET_THEME',
  SET_SYSTEM_THEME: 'SET_SYSTEM_THEME',
  SET_CONNECTED: 'SET_CONNECTED',
  SET_PING_TIMER: 'SET_PING_TIMER',
  SET_STATUS_MSG: 'SET_STATUS_MSG',
  SET_HOST_HEALTH: 'SET_HOST_HEALTH',
  SET_HOSTS: 'SET_HOSTS',
  SET_HOST_CONFIG: 'SET_HOST_CONFIG',
  SET_FILTERED_HOSTS: 'SET_FILTERED_HOSTS',
  PUSH_NOTIFICATION: 'PUSH_NOTIFICATION',
  SET_READ_ALL: 'SET_READ_ALL',
  SET_HOST_ID: 'SET_HOST_ID',
  ADD_SIMULATED_HOSTS: 'ADD_SIMULATED_HOSTS',
});

const notificationTypes = Object.freeze({
  SUCCESS: 'SUCCESS',
  INFO: 'INFO',
  WARN: 'WARN',
  ERROR: 'ERROR',
});

const notifications = Object.freeze({
  ECONN_FAILED: () => ({
    type: notificationTypes.ERROR,
    title: 'Connection Failed',
    desc: 'Connection to bridge server failed',
  }),
  EFETCH_HOSTS_FAILED: () => ({
    type: notificationTypes.ERROR,
    title: 'Could not fetch hosts',
    desc: 'Could not fetch hosts',
  }),
  WCONN_DISCONNECTED: () => ({
    type: notificationTypes.WARN,
    title: 'Disconnected',
    desc: 'Disconnected from bridge server',
  }),
  EFETCH_HOST_CONFIG_FAILED: () => ({
    type: notificationTypes.ERROR,
    title: 'Could not fetch host config',
    desc: 'Could not fetch host config',
  }),
  EUPDATE_HOST_PROP_FAILED: (clientName, name, value) => ({
    type: notificationTypes.ERROR,
    title: 'Could not update host property',
    desc: `Could not update host [${clientName}] property [${name}] to value [${value}]`,
  }),
  WHOST_OFFLINE: (clientName) => ({
    type: notificationTypes.WARN,
    title: 'Host is offline',
    desc: `Host [${clientName}] is offline`,
  }),
  IHOST_ID_CHANGED: (clientName) => ({
    type: notificationTypes.INFO,
    title: 'Host id changed',
    desc: `Host id for host [${clientName}] has been changed`,
  }),
});

export { themes, themeName, mutationKeys, notificationTypes, notifications, apiURL };
