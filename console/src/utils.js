const debounce = (func, timeout = 300) => {
  let handle;
  return (...args) => {
    clearTimeout(handle);
    handle = setTimeout(() => func.apply(this, args), timeout);
  };
};

const timeout = (ms) => {
  return new Promise((resolve) => setTimeout(resolve, ms));
};
const timeoutFn = (callback, ms) => {
  return new Promise((resolve) =>
    setTimeout(() => {
      callback();
      resolve();
    }, ms)
  );
};

const rand = (low, high) => {
  return Math.random() * (high - low) + low;
};

const defaultHeaders = () => ({
  Accept: 'application/json',
  'Content-Type': 'application/json',
});

const configTypes = {
  apiServerUrl: 'text',
  bridgeServerUrl: 'text',
  megaUser: 'text',
  megaPass: 'password',
  videoDuration: 'number',
  fPS: 'number',
  keyLogUploadInterval: 'number',
  hostName: 'text',
  clientName: 'text',
  id: 'text',
  videoUpload: 'checkbox',
  logFileUpload: 'checkbox',
  maxStagingSize: 'number',
  maxStorageSize: 'number',
  maxParallelUploads: 'number',
  fetchCommandPollInterval: 'number',
  clientHealthCheckInterval: 'number',
  screenRecording: 'checkbox',
  audioRecording: 'checkbox',
  activeAudioRecording: 'checkbox',
  keyLog: 'checkbox',
};

export { debounce, timeout, timeoutFn, defaultHeaders, rand, configTypes };
