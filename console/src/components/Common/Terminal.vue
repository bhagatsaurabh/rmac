<template>
  <div ref="termEl" class="terminal"></div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Terminal as XTerm } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
const fitAddon = new FitAddon();

defineProps({
  id: {
    type: Number,
    required: true,
  },
});

const termEl = ref(null);

const terminal = new XTerm();
terminal.loadAddon(fitAddon);

onMounted(() => {
  terminal.open(termEl.value);
  fitAddon.fit();
});
</script>

<style scoped>
.terminal {
  width: 100%;
  height: calc(100% - 5rem - 3px);
  background-color: var(--c-black);
}
</style>
