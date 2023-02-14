<template>
  <div class="toast">
    <Icon alt="Notification type icon" :name="`icons/action-${data.type.toLowerCase()}`" singular />
    {{ data.title }}
    {{ data.desc }}
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
}
</style>
