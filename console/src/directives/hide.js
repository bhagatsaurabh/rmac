export default (el, binding) => {
  el.style.opacity = !!binding.value ? '0' : '1';
  el.style.pointerEvents = !!binding.value ? 'none' : 'all';
};
