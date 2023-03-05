<template>
  <div class="commands">
    <p class="mb-0p5">Commands</p>
    <Button @click="emit('command', 'panic')">panic</Button>
    <Button @click="emit('command', 'system shutdown')">shutdown</Button>
    <Button @click="emit('command', 'process list')">process list</Button>
  </div>
</template>

<script setup>
import Button from './Common/Button.vue';

const emit = defineEmits(['command']);
</script>

<style scoped>
.commands {
  padding: 0.5rem 1rem 1rem 1rem;
  border-bottom: 1px solid var(--c-border-soft);
}
.commands:deep(button) {
  display: inline-block;
  padding: 0.25rem 0.5rem;
}
.commands:deep(button:not(:last-child)) {
  margin-right: 0.5rem;
}
</style>
