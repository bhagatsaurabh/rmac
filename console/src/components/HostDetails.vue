<template>
  <section>
    <header>
      <Icon alt="Host icon" name="icons/host" adaptive :size="1.4" />
      <h2>Properties</h2>
    </header>
    <div class="host-content">
      <Property :id="host.id" name="clientName" editable>Client Name</Property>
      <Property :id="host.id" name="hostName" editable>Host Name</Property>
      <Property :id="host.id" name="id">ID</Property>
      <h3>{{ host.health ? 'Online' : 'Offline' }}</h3>
      <h3>{{ host.registered ? 'Registered' : 'Unknown' }}</h3>
    </div>
  </section>
</template>

<script setup>
import Icon from './Common/Icon.vue';
import Property from './Common/Property.vue';

defineProps({
  host: {
    type: Object,
    required: true,
  },
});
</script>

<style scoped></style>
