<template>
  <section>
    <header>
      <Icon alt="Config icon" name="icons/config" adaptive :size="1.4" />
      <h2>Config</h2>
    </header>
    <div class="host-content">
      <h3>{{ host.id }}</h3>
      <h3>{{ host.clientName }}</h3>
      <h3>{{ host.hostName }}</h3>
      <h3>{{ host.health ? 'Online' : 'Offline' }}</h3>
      <h3>{{ host.registered ? 'Registered' : 'Unknown' }}</h3>
    </div>
  </section>
</template>

<script setup>
import Icon from './Common/Icon.vue';

defineProps({
  host: {
    type: Object,
    required: true,
  },
});
</script>

<style scoped></style>
