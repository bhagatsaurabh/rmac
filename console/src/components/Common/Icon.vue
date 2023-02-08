<template>
  <span class="icon-container">
    <img
      v-hide="!adaptive && theme === themes.DARK"
      :alt="alt"
      :style="{ ...config }"
      :class="adaptive ? 'icon-adaptive' : 'icon'"
      :src="lightSource"
    />
    <img
      v-if="!adaptive"
      v-hide="theme === themes.LIGHT"
      :alt="alt"
      :style="{ ...config }"
      class="icon dark"
      :src="darkSource"
    />
  </span>
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
  adaptive: {
    type: Boolean,
    default: false,
  },
  config: {
    type: Object,
    default: { maxWidth: '1rem' },
    validator({ maxHeight, maxWidth }) {
      if (!((maxHeight != null) ^ (maxWidth != null))) return false;

      return true;
    },
  },
});

const store = useStore();
const theme = computed(() => store.getters.theme);

const metaUrl = import.meta.url;
const lightSource = new URL(`../../assets/${props.name}.png`, metaUrl).href;
let darkSource;
if (!props.adaptive) {
  darkSource = new URL(`../../assets/${props.name}-dark.png`, metaUrl).href;
}
</script>

<style scoped>
.icon-container {
  vertical-align: -18%;
}
.icon {
  transition: opacity var(--theme-transition-duration) linear;
}

.icon-adaptive {
  transition: filter var(--theme-transition-duration) linear;
}
html:not([data-theme='light']) .icon-adaptive {
  filter: invert(1);
}

.icon.dark {
  position: absolute;
  top: 0;
  left: 0;
}
</style>
