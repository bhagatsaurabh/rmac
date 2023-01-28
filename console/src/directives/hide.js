export default (el, binding) => {
  el.style.opacity = !!binding.value ? '0' : '1';
};
