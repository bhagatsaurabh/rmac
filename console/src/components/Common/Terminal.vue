<template>
  <div ref="termEl" class="terminal"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { Terminal as XTerm } from 'xterm';
import { debounce } from '@/utils';

defineProps({
  id: {
    type: Number,
    required: true,
  },
});

const termEl = ref(null);

const terminal = new XTerm();

const resize = () => {
  if (!termEl.value) return;

  const computedStyle = window.getComputedStyle(termEl.value);
  const availableHeight = parseInt(computedStyle.getPropertyValue('height'));
  const availableWidth = parseInt(computedStyle.getPropertyValue('width'));
  const cellHeight = terminal._core._renderService.dimensions.css.cell.height;
  const cellWidth = terminal._core._renderService.dimensions.css.cell.width;

  terminal.resize(
    Math.max(1, Math.floor(availableWidth / cellWidth)),
    Math.max(1, Math.floor(availableHeight / cellHeight))
  );
};

const observer = new ResizeObserver(debounce(resize, 150));

onMounted(() => {
  terminal.open(termEl.value);
  resize();
  observer.observe(termEl.value);
});
onBeforeUnmount(() => {
  observer.unobserve(termEl.value);
});
</script>

<style scoped>
.terminal {
  width: calc(100% - 2rem);
  height: 100%;
  background-color: var(--c-black);
  z-index: 2;
}

@media (min-width: 1024px) {
  .terminal {
    width: calc(100% - 3rem);
  }
}
</style>
