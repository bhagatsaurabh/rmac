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
      <div class="statuses">
        <div class="status">
          <span class="field-name">Connectivity:</span>
          <Icon
            singular
            alt="Offline icon"
            :name="`icons/${host.health ? 'action-success' : 'offline'}`"
          />
          <span>{{ host.health ? 'Online' : 'Offline' }}</span>
        </div>
        <div class="status">
          <span class="field-name">Registration:</span>
          <Icon
            singular
            alt="Registered icon"
            :name="`icons/${host.registered ? 'action-success' : 'action-warn'}`"
          />
          <span>{{ host.registered ? 'Registered' : 'Unknown' }}</span>
        </div>
      </div>
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

<style scoped>
.status:deep(.icon-container) {
  vertical-align: middle;
  margin-right: 0.5rem;
}
.status .field-name {
  display: inline-block;
  width: 6rem;
}
.statuses {
  margin-top: 2rem;
  padding: 0 0.5rem;
}
</style>
