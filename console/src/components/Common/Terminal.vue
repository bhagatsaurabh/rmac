<template>
  <div ref="termEl" class="terminal"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { Terminal as XTerm } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import { debounce } from '@/utils';
import { useStore } from 'vuex';
import { emit } from '@/socket';
import bus from '@/event';

const store = useStore();

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
  host: {
    type: Object,
    required: true,
  },
});

const termEl = ref(null);

const terminal = new XTerm({ cursorBlink: true });
const fitAddon = new FitAddon();
terminal.loadAddon(fitAddon);

terminal.onData((data) => {
  emit({
    event: 'terminal:data',
    type: 'console',
    data,
    rayId: `${props.host.id}:${props.id}`,
  });
});
terminal.onResize((e) => {
  emit({
    event: 'terminal:resize',
    type: 'console',
    data: e,
    rayId: `${props.host.id}:${props.id}`,
  });
});

const observer = new ResizeObserver(debounce(() => termEl.value && fitAddon.fit(), 150));

onMounted(() => {
  terminal.open(termEl.value);
  fitAddon.fit();
  observer.observe(termEl.value);

  bus.on(`${props.host.id}:${props.id}`, (data) => {
    terminal.write(data);
  });

  store.dispatch('openTerminal', { hostId: props.host.id, terminalId: props.id });
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
