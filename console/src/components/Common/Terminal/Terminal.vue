<template>
  <div class="terminal">
    <div class="terminal-window" ref="termEl">
      <Button
        @click="() => componentEmit('close')"
        class="control-disconnect"
        icon="disconnect"
        icon-left
      >
        Disconnect
      </Button>
    </div>
    <div class="terminal-status">
      <span>Terminal {{ idx + 1 }}: </span>
      <span :class="state">{{ status }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { useStore } from 'vuex';
import { Terminal as XTerm } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import Button from '../Button/Button.vue';
import { debounce, rand } from '@/utils';
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
  idx: {
    type: Number,
    required: true,
  },
});
const componentEmit = defineEmits(['close']);

const isSimulated = computed(() => props.host.id.startsWith('sim-'));
const simHandle = ref(-1);
const termEl = ref(null);
const state = ref('closed');
const status = computed(() => {
  if (state.value === 'closed') return 'Disconnected';
  else if (state.value === 'requested') return 'Waiting for host...';
  else if (state.value === 'opened') return 'Connected';
  else return 'Unknown';
});

const terminal = new XTerm({ cursorBlink: true });
const fitAddon = new FitAddon();
terminal.loadAddon(fitAddon);

terminal.onData((data) => {
  if (isSimulated.value) {
    return;
  }

  emit({
    event: 'terminal:data',
    type: 'console',
    data,
    rayId: `${props.host.id}:${props.id}`,
  });
});
terminal.onResize((e) => {
  if (isSimulated.value) {
    return;
  }

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
    if (state.value !== 'opened') state.value = 'opened';
    terminal.write(data);
  });

  store.dispatch('openTerminal', { hostId: props.host.id, terminalId: props.id });
  if (isSimulated.value) {
    simHandle.value = setTimeout(() => {
      state.value = 'opened';
      terminal.write('This is a Simulated Host >');
    }, rand(500, 1500));
  }
  state.value = 'requested';
});

onBeforeUnmount(() => {
  clearTimeout(simHandle.value);
  state.value = 'closed';
  terminal.dispose();
  observer.unobserve(termEl.value);
  store.dispatch('closeTerminal', { hostId: props.host.id, terminalId: props.id });
});
</script>

<style scoped>
.terminal {
  width: calc(100% - 2rem);
  height: 100%;
  z-index: 2;
  background-color: #000;
}
.terminal .terminal-window {
  width: 100%;
  height: calc(100% - 1.5rem);
}
.terminal .terminal-status {
  width: 100%;
  height: 1.5rem;
  background-color: var(--c-text-soft);
  color: var(--c-background);
  padding: 0 0.5rem;
}

.terminal-status .opened {
  background-color: rgb(0, 189, 0);
  padding: 0 0.5rem;
}
.terminal-status .requested {
  background-color: rgb(240, 240, 64);
  color: var(--c-black);
  padding: 0 0.5rem;
}
.terminal-status .closed {
  background-color: rgb(255, 73, 73);
  padding: 0 0.5rem;
}
.terminal-window .control-disconnect {
  position: absolute;
  z-index: 5;
  right: 0;
  top: 0;
  border-radius: 0;
  box-shadow: none;
  padding: 0.2rem 0.5rem;
}

@media (min-width: 1024px) {
  .terminal {
    width: calc(100% - 3rem);
  }
}
</style>
