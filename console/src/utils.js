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

const defaultHeaders = () => ({
  Accept: 'application/json',
  'Content-Type': 'application/json',
});

export { debounce, timeout, defaultHeaders };
