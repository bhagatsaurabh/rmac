<template>
  <div class="notification">
    <Icon alt="Notification type icon" :name="`icons/action-${data.type.toLowerCase()}`" />
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

<style scoped></style>
