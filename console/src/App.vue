<template>
  <Header>
    <template #left>
      <Logo alt="RMAC logo" name="rmac-logo-spell" :config="{ maxHeight: '1.5rem' }" />
    </template>
    <template #right>
      <Notifications />
      <ThemeSelector />
    </template>
  </Header>
  <RouterView />
  <Footer />
</template>

<script setup>
import { useStore } from 'vuex';
import { onBeforeUnmount, computed } from 'vue';

import { themes } from '@/store/constants';
import ThemeSelector from '@/components/Common/ThemeSelector.vue';
import Footer from './components/Common/Footer.vue';
import Notifications from './components/Common/Notifications.vue';
import Header from './components/Common/Header.vue';
import Logo from './components/Common/Logo.vue';

const store = useStore();
store.dispatch('loadPreferences');
const currTheme = computed(() => store.state.preferences.theme);

const mediaChangeHandler = async (e) => {
  if (e.matches && currTheme.value === themes.SYSTEM) {
    await store.dispatch('setTheme', themes.SYSTEM);
  }
};
const mediaHighCntrst = window.matchMedia('(prefers-contrast: more)');
mediaHighCntrst.addEventListener('change', mediaChangeHandler);
const mediaDark = window.matchMedia('(prefers-color-scheme: dark)');
mediaDark.addEventListener('change', mediaChangeHandler);
const mediaLight = window.matchMedia('(prefers-color-scheme: light)');
mediaLight.addEventListener('change', mediaChangeHandler);
onBeforeUnmount(() => {
  mediaHighCntrst.removeEventListener('change', mediaChangeHandler);
  mediaDark.removeEventListener('change', mediaChangeHandler);
  mediaLight.removeEventListener('change', mediaChangeHandler);
});
</script>

<style scoped></style>
