<template>
  <span class="icon-container">
    <img
      v-hide="!adaptive && theme === themes.DARK"
      :alt="alt"
      :style="{ ...config, maxWidth: `${size}rem` }"
      :class="{
        'icon-adaptive': adaptive,
        icon: !adaptive,
        invert,
      }"
      :src="lightSource"
    />
    <img
      v-if="!adaptive"
      v-hide="theme === themes.LIGHT"
      :alt="alt"
      :style="{ ...config, maxWidth: `${size}rem` }"
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
  size: {
    type: Number,
    default: 1,
  },
  invert: {
    type: Boolean,
    default: false,
  },
  config: {
    type: Object,
    default: {},
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
.icon-adaptive.invert {
  filter: invert(1);
}
html[data-theme='dark'] .icon-adaptive:not(.invert) {
  /* #dddddd */
  filter: invert(93%) sepia(0%) saturate(2975%) hue-rotate(147deg) brightness(126%) contrast(73%);
}
html[data-theme='dark'] .icon-adaptive.invert {
  /* #222222 */
  filter: invert(9%) sepia(0%) saturate(509%) hue-rotate(170deg) brightness(93%) contrast(87%);
}
html[data-theme='high-contrast'] .icon-adaptive:not(.invert) {
  filter: invert(1);
}
html[data-theme='high-contrast'] .icon-adaptive.invert {
  filter: invert(0);
}

.icon.dark {
  position: absolute;
  top: 0;
  left: 0;
}
</style>
