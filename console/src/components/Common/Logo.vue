<template>
  <!-- <Transition name="fade">
    <img
      v-if="currTheme === themes.LIGHT"
      :alt="alt"
      :style="{ ...config }"
      class="logo"
      :src="lightSource"
    />
  </Transition>
  <Transition>
    <img :alt="alt" :style="{ ...config }" class="logo dark" :src="darkSource" />
  </Transition> -->
  <div>
    <img
      v-hide="currTheme === themes.DARK"
      :alt="alt"
      :style="{ ...config }"
      class="logo"
      :src="lightSource"
    />
    <img
      v-hide="currTheme === themes.LIGHT"
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
const currTheme = computed(() => store.getters.getTheme);

const lightSource = new URL(`../../assets/${props.name}.png`, import.meta.url).href;
const darkSource = new URL(`../../assets/${props.name}-dark.png`, import.meta.url).href;
</script>

<style scoped>
.logo {
  transition: opacity var(--theme-transition-duration) linear;
}

.dark {
  position: absolute;
  top: 0;
  left: 0;
}
</style>
