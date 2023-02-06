<template>
  <TransitionGroup class="theme-selector" name="themes" tag="div">
    <button class="theme-item" v-for="theme in themesList" :key="theme">
      {{ themeName(theme) }}
    </button>
  </TransitionGroup>
</template>

<script setup>
import { themeName, themes } from '@/store/constants';
import { computed, watch, ref } from 'vue';
import { useStore } from 'vuex';

const store = useStore();

let themesList = ref(Object.values(themes));

const currTheme = computed(() => store.state.preferences.theme);

watch(
  currTheme,
  () => {
    const currThemeIdx = themesList.value.findIndex((val) => val === currTheme.value);
    if (currThemeIdx === 0) return;

    [themesList.value[0], themesList.value[currThemeIdx]] = [
      themesList.value[currThemeIdx],
      themesList.value[0],
    ];
  },
  { immediate: true }
);
</script>

<style scoped>
.theme-selector {
  z-index: 5;
  position: absolute;
  top: 0;
  right: 0;
  direction: rtl;
}

.theme-item {
  display: block;
}

.themes-move,
.themes-enter-active,
.themes-leave-active {
  transition: all 0.5s ease;
}

.themes-enter-from,
.themes-leave-to {
  opacity: 0;
  /* transform: translateX(30px); */
}

.themes-leave-active {
  position: absolute;
}
</style>
