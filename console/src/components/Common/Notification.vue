<template>
  <div class="notification">
    <div class="header">
      <Icon
        alt="Notification type icon"
        :name="`icons/action-${data.type.toLowerCase()}`"
        singular
      />
      <span>{{ data.title }}</span>
    </div>
    <div class="content">
      <span>{{ data.desc }}</span>
    </div>
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
.notification {
  padding: 0.5rem 1rem;
  background-color: var(--c-background-mute);
  margin: 0.5rem;
  box-shadow: 0px 4px 12px -5px var(--c-shadow);
  border: 1px solid var(--c-box-border);
}
.notification .header {
  border-bottom: 1px solid var(--c-shadow-soft);
  padding-bottom: 0.5rem;
}
.notification .header span {
  vertical-align: middle;
  font-weight: bold;
}
.notification .header .icon-container {
  margin-right: 0.5rem;
  font-size: 0;
}
.notification .content {
  margin-top: 0.5rem;
}
</style>
