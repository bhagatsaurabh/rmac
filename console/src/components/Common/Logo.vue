<template>
  <div class="logo-container">
    <img
      v-hide="theme !== themes.LIGHT"
      :alt="alt"
      :style="{ ...config }"
      class="logo"
      :src="lightSource"
    />
    <img
      v-hide="theme === themes.LIGHT"
      :alt="alt"
      :style="{ ...config }"
      class="logo dark"
      :src="darkSource"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useStore } from 'vuex';
import { themes } from '@/store/constants';

const props = defineProps({
  alt: {
    type: String,
    required: true,
  },
  name: {
    type: String,
    required: true,
  },
  animated: {
    type: Boolean,
    default: false,
  },
  config: {
    type: Object,
    required: true,
    validator({ maxHeight, maxWidth }) {
      if (!((maxHeight != null) ^ (maxWidth != null))) return false;

      return true;
    },
  },
});

const store = useStore();
const theme = computed(() => store.getters.theme);

const metaUrl = import.meta.url;
const lightSource = new URL(`/assets/${props.name}.${props.animated ? 'gif' : 'png'}`, metaUrl)
  .href;
const darkSource = new URL(`/assets/${props.name}-dark.${props.animated ? 'gif' : 'png'}`, metaUrl)
  .href;
</script>

<style scoped>
.logo-container {
  max-width: fit-content;
}
.logo {
  transition: opacity var(--theme-transition-duration) linear;
}

.dark {
  position: absolute;
  top: 0;
  left: 0;
}
</style>
