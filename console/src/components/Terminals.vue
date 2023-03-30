<template>
  <aside :class="{ terminals: true, open: isOpen }">
    <button @click="() => (isOpen = !isOpen)" class="header">
      <div class="left">
        <Icon alt="Terminal icon" name="icons/terminal" adaptive :size="1.4" />
        <h2>Terminal</h2>
      </div>
      <Icon class="chevron" alt="Chevron icon" name="icons/right-chevron" adaptive :size="1.4" />
    </button>
    <Transition name="expand">
      <div v-if="isOpen" class="terminals-content">
        <Commands @command="handleCommand" />
        <div class="terminals-view">
          <div v-if="terminals.length === 0" class="no-terminal">
            <pre>Connect a new terminal with <span class="font-size-2p5">+</span> icon</pre>
          </div>
          <Terminal
            v-for="id in terminals"
            :key="id"
            :id="id"
            v-show="id === activeTerminal"
            :host="host"
          />
          <TerminalNav
            @select="handleSelect"
            @add="handleAdd"
            :terminals="terminals"
            :active="activeTerminal"
          />
        </div>
      </div>
    </Transition>
  </aside>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue';
import Commands from './Commands.vue';
import Icon from './Common/Icon.vue';
import { Transition } from 'vue';
import Terminal from './Common/Terminal.vue';
import TerminalNav from './Common/TerminalNav.vue';
import { v4 as uuid } from 'uuid';
import { useStore } from 'vuex';
import bus from '@/event';
import { notifications } from '@/store/constants';

const props = defineProps({
  host: {
    type: Object,
    required: true,
  },
});

const store = useStore();

const isOpen = ref(false);
const defaultTermId = uuid();
const terminals = ref([defaultTermId]);
const activeTerminal = ref(defaultTermId);

const handleCommand = () => {};
const handleSelect = (id) => {
  activeTerminal.value = id;
};
const handleAdd = () => {
  const newTermId = uuid();
  terminals.value = [...terminals.value, newTermId];
  handleSelect(newTermId);
};

bus.on('terminal:close', async (rayId) => {
  const [hostId, terminalId] = rayId.split(':');
  if (hostId === props.host.id && terminals.value.includes(terminalId)) {
    const index = terminals.value.indexOf(terminalId);
    const updatedTerminals = [...terminals.value];
    updatedTerminals.splice(index, 1);
    terminals.value = updatedTerminals;
    bus.emit('notify', notifications.WTERMINAL_DISCONNECTED(index + 1));
  }
});

onBeforeUnmount(() => {
  terminals.value.forEach((terminalId) => {
    store.dispatch('closeTerminal', { hostId: props.host.id, terminalId });
  });
});
</script>

<style scoped>
.no-terminal {
  width: calc(100% - 2rem);
  height: 100%;
  z-index: 2;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.5rem;
}
.terminals {
  position: sticky;
  bottom: 0;
  box-shadow: 0 -6px 6px -4px var(--c-shadow);
  margin-top: 1rem;
  z-index: 1;
}
.terminals .header {
  margin-bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 4rem;
  width: 100%;
  background-color: var(--c-background);
  border: 1px solid var(--c-box-border);
  padding: 1rem;
  z-index: 1;
  cursor: pointer;
  transition: background-color var(--fx-transition-duration) linear;
}
.terminals .header .icon-container {
  font-size: 0;
  margin-right: 1rem;
}
.terminals .header h2 {
  display: inline-block;
  color: var(--c-text);
}
.terminals-content {
  overflow: hidden;
  border-top: 1px solid var(--c-border-soft);
  transition: height var(--fx-transition-duration-slower) ease;
  background-color: var(--c-background);
  height: min(50vh, 18rem);
}
.terminals .chevron {
  transform: rotateZ(-90deg);
  margin-left: 1rem;
  justify-self: end;
  margin-right: 0 !important;
  transition: transform var(--fx-transition-duration-slower) ease;
}
.terminals.open .chevron {
  transform: rotateZ(90deg);
}
.header .left {
  display: flex;
  align-items: center;
}

.expand-enter-active,
.expand-leave-active {
  transition: height var(--fx-transition-duration-slower) ease;
  height: min(50vh, 18rem);
}

.expand-enter-from,
.expand-leave-to {
  height: 0rem;
}

.expand-enter-to,
.expand-leave-from {
  height: min(50vh, 18rem);
}

.terminals-view {
  width: 100%;
  height: calc(100% - 5rem - 3px);
  display: flex;
  overflow: hidden;
}

@media (hover: hover) {
  .terminals .header:hover {
    background-color: var(--c-background-soft);
  }
}

@media (min-width: 1024px) {
  .no-terminal {
    width: calc(100% - 3rem);
  }
}
</style>
