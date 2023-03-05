const debounce = (func, timeout = 300) => {
  let handle;
  return (...args) => {
    clearTimeout(handle);
    handle = setTimeout(() => func.apply(this, args), timeout);
  };
};

export { debounce };
