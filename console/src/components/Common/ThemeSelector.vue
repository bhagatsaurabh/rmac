<template>
  <div class="theme-selector">
    <button v-for="theme in themes">{{ themeName(theme) }}</button>
  </div>
</template>

<script setup>
import { themeName, themes } from '@/store/constants';
</script>

<style scoped>
.theme-selector {
  z-index: 5;
  position: absolute;
  top: 0;
  right: 0;
}
</style>
