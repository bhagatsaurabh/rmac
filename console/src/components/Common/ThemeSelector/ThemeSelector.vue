<template>
  <TransitionGroup :class="{ 'theme-selector': true, open: isOpen }" name="themes" tag="nav">
    <button
      :data-label="themeLabel(theme)"
      :class="{ 'theme-item': true, active: currTheme === theme }"
      v-for="(theme, index) in themesList"
      :key="theme"
      :style="{ top: `${index * 3.5}rem` }"
      @click="themeSelectHandler(theme)"
      :tabindex="currTheme === theme || isOpen ? '0' : '-1'"
      ref="themeButtons"
      @blur="currTheme === theme && activeThemeBlurHandler()"
    >
      <Icon
        :alt="`${themeName(theme)} theme icon`"
        :name="`icons/theme-${theme}`"
        :size="2"
        adaptive
      />
    </button>
  </TransitionGroup>
</template>

<script setup>
import { themeName, themes } from '@/store/constants';
import { computed, watch, ref } from 'vue';
import { useStore } from 'vuex';
import Icon from '../Icon/Icon.vue';

const store = useStore();

let themesList = ref(Object.values(themes));
const isOpen = ref(false);
const themeButtons = ref([]);

const currTheme = computed(() => store.state.preferences.theme);
const sysTheme = computed(() => store.state.preferences.sysTheme);
const themeLabel = (theme) => {
  const tName = themeName(theme);
  return tName + (tName === 'System' ? ` (${sysTheme.value})` : '');
};

let handle;
const themeSelectHandler = async (theme) => {
  if (currTheme.value !== theme) {
    await store.dispatch('setTheme', theme);
    isOpen.value = false;
    clearTimeout(handle);
  } else {
    isOpen.value = !isOpen.value;
  }

  if (!isOpen.value) {
    themeButtons.value.find((themeButton) => themeButton.classList.contains('active')).blur();
  }
};
const activeThemeBlurHandler = () => {
  clearTimeout(handle);
  handle = setTimeout(() => (isOpen.value = false), 70);
};

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
  position: relative;
  direction: rtl;
  color: var(--c-text);
  width: 3rem;
  height: 3rem;
}
.theme-selector:not(.open) .theme-item {
  top: 0rem !important;
  opacity: 0;
}

.theme-item {
  color: var(--c-text);
  border: 1px solid var(--c-box-border);
  display: block;
  transition: all var(--fx-transition-duration) ease;
  background-color: var(--c-background);
  font-size: 0;
  border-radius: 999px;
  box-shadow: 0 0 10px 0 var(--c-shadow);
  padding: 0.5rem;
  position: absolute;
  top: 0rem;
  z-index: 1;
  cursor: pointer;
}
.theme-item:active {
  box-shadow: 0 0 10px -2px var(--c-shadow), 4px 4px 10px -5px var(--c-shadow-soft) inset;
}
.theme-item.active {
  z-index: 2;
  opacity: 1 !important;
}

.theme-item:not(:last-child) {
  margin-bottom: 0.5rem;
}
.theme-item::before {
  content: attr(data-label);
  opacity: 0;
  font-size: 1rem;
  position: absolute;
  white-space: nowrap;
  top: 50%;
  right: calc(100% + 1rem);
  transform: translate(10%, -50%);
  transition: transform var(--fx-transition-duration) linear,
    opacity var(--fx-transition-duration) linear;
  padding: 0.4rem 1.2rem;
  pointer-events: none;
  background-color: var(--c-background-mute);
  border-radius: 999px;
  border: 1px solid var(--c-box-border);
  box-shadow: 0 0 8px -1px var(--c-shadow);
}

.theme-item:focus::before,
.theme-selector.open .theme-item::before {
  opacity: 1;
  transform: translate(0, -50%);
  outline: none;
}
@media (hover: hover) {
  .theme-item:hover::before {
    opacity: 1;
    transform: translate(0, -50%);
    outline: none;
  }
}

.themes-move,
.themes-enter-active,
.themes-leave-active {
  transition: all 0.5s ease;
}
.themes-enter-from,
.themes-leave-to {
  opacity: 0;
}
.themes-leave-active {
  position: absolute;
}
</style>
