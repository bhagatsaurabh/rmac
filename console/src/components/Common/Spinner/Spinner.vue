<template>
  <Icon
    class="spinner"
    alt="Spinner icon"
    name="icons/spinner"
    :invert="invert"
    :size="size"
    adaptive
  />
</template>

<script setup>
import Icon from '../Icon/Icon.vue';

defineProps({
  size: {
    type: Number,
    default: 1,
  },
  invert: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.spinner:deep(img) {
  animation-name: spin;
  animation-timing-function: linear;
  animation-duration: 1s;
  animation-iteration-count: infinite;
  animation-fill-mode: forwards;
}
</style>
