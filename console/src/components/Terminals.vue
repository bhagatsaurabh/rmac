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
          <Terminal :id="activeTerminal" :host="host" />
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
import { ref } from 'vue';
import Commands from './Commands.vue';
import Icon from './Common/Icon.vue';
import { Transition } from 'vue';
import Terminal from './Common/Terminal.vue';
import TerminalNav from './Common/TerminalNav.vue';

defineProps({
  host: {
    type: Object,
    required: true,
  },
});

const isOpen = ref(false);
const terminals = ref(1);
const activeTerminal = ref(1);

const handleCommand = () => {};
const handleSelect = (id) => {
  activeTerminal.value = id;
};
const handleAdd = () => {
  terminals.value += 1;
  handleSelect(terminals.value);
};
</script>

<style scoped>
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
</style>
