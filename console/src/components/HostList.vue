<template>
  <section class="hosts-list">
    <HostCard v-for="host in hosts" :key="host.id" :host="host" />
  </section>
</template>

<script setup>
import HostCard from './HostCard.vue';

defineProps({
  hosts: {
    type: Object,
    required: true,
  },
});
</script>

<style scoped>
.hosts-list {
  display: flex;
  flex-wrap: wrap;
}

.hosts-list a {
  text-decoration: none;
}
</style>
