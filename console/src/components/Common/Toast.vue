<template>
  <div class="toast">
    <Icon alt="Notification type icon" :name="`icons/action-${data.type.toLowerCase()}`" singular />
    <em>{{ data.title }}</em>
  </div>
</template>

<script setup>
import Icon from './Icon.vue';

defineProps({
  data: {
    type: Object,
    required: true,
  },
});
</script>

<style scoped>
.toast {
  position: fixed;
  cursor: pointer;
  padding: 0.5rem 1rem;
  opacity: 0.9;
  bottom: 1rem;
  width: 80vw;
  left: 10vw;
  background-color: var(--c-background-soft);
  box-shadow: 0 0 10px 0px var(--c-shadow);
  border: 1px solid var(--c-box-border);
  border-radius: 0.5rem;
  overflow-wrap: break-word;
}
.toast em {
  vertical-align: middle;
}

.toast:deep(.icon) {
  margin-right: 0.5rem;
}

@media (min-width: 1024px) {
  .toast {
    width: 50vw;
    left: 25vw;
  }
}
</style>
