<template>
  <aside :class="{ terminal: true, open: isOpen }">
    <button @click="() => (isOpen = !isOpen)" class="header">
      <div class="left">
        <Icon alt="Terminal icon" name="icons/terminal" adaptive :size="1.4" />
        <h2>Terminal</h2>
      </div>
      <Icon class="chevron" alt="Chevron icon" name="icons/right-chevron" adaptive :size="1.4" />
    </button>
    <Transition name="expand">
      <div v-if="isOpen" class="terminal-content">
        <Commands />
        <h3>{{ host.id }}</h3>
        <h3>{{ host.clientName }}</h3>
        <h3>{{ host.hostName }}</h3>
        <h3>{{ host.health ? 'Online' : 'Offline' }}</h3>
        <h3>{{ host.registered ? 'Registered' : 'Unknown' }}</h3>
      </div>
    </Transition>
  </aside>
</template>

<script setup>
import { ref } from 'vue';
import Commands from './Commands.vue';
import Icon from './Common/Icon.vue';
import { Transition } from 'vue';

defineProps({
  host: {
    type: Object,
    required: true,
  },
});

const isOpen = ref(false);
</script>

<style scoped>
.terminal {
  position: sticky;
  bottom: 0;
  box-shadow: 0 -6px 6px -4px var(--c-shadow);
  margin-top: 1rem;
  z-index: 1;
}
.terminal .header {
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
.terminal .header .icon-container {
  font-size: 0;
  margin-right: 1rem;
}
.terminal .header h2 {
  display: inline-block;
}
.terminal-content {
  overflow: hidden;
  border-top: 1px solid var(--c-border-soft);
  transition: height var(--fx-transition-duration-slower) ease;
  background-color: var(--c-background);
  height: min(50vh, 15rem);
}
.terminal .chevron {
  transform: rotateZ(-90deg);
  margin-left: 1rem;
  justify-self: end;
  margin-right: 0 !important;
  transition: transform var(--fx-transition-duration-slower) ease;
}
.terminal.open .chevron {
  transform: rotateZ(90deg);
}
.header .left {
  display: flex;
  align-items: center;
}

.expand-enter-active,
.expand-leave-active {
  transition: height var(--fx-transition-duration-slower) ease;
  height: min(50vh, 15rem);
}

.expand-enter-from,
.expand-leave-to {
  height: 0rem;
}

.expand-enter-to,
.expand-leave-from {
  height: min(50vh, 15rem);
}

@media (hover: hover) {
  .terminal .header:hover {
    background-color: var(--c-background-soft);
  }
}
</style>
