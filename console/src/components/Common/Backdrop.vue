<template>
  <Transition name="backdrop">
    <div v-if="show" @click="$emit('dismiss')" class="backdrop"></div>
  </Transition>
</template>

<script setup>
defineProps({
  show: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
.backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 101;
  opacity: 1;
  /* transition: opacity 0.1s linear; */
  background-color: #8383834d;
  backdrop-filter: blur(5px);
}

.backdrop-enter-active,
.backdrop-leave-active {
  transition: opacity 0.3s ease;
}

.backdrop-enter-from,
.backdrop-leave-to {
  opacity: 0;
}
</style>
