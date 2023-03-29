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
  ApiServerUrl: 'text',
  BridgeServerUrl: 'text',
  MegaUser: 'text',
  MegaPass: 'password',
  VideoDuration: 'number',
  FPS: 'number',
  KeyLogUploadInterval: 'number',
  HostName: 'text',
  ClientName: 'text',
  Id: 'text',
  VideoUpload: 'checkbox',
  LogFileUpload: 'checkbox',
  MaxStagingSize: 'number',
  MaxStorageSize: 'number',
  MaxParallelUploads: 'number',
  FetchCommandPollInterval: 'number',
  ClientHealthCheckInterval: 'number',
  ScreenRecording: 'checkbox',
  AudioRecording: 'checkbox',
  ActiveAudioRecording: 'checkbox',
  KeyLog: 'checkbox',
};

export { debounce, timeout, timeoutFn, defaultHeaders, rand, configTypes };
