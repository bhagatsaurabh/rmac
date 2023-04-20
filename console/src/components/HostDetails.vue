<template>
  <section>
    <header>
      <Icon alt="Host icon" name="icons/host" adaptive :size="1.4" />
      <h2>Properties</h2>
    </header>
    <div class="host-content">
      <Property :id="host.id" type="global" name="clientName" editable>Client Name</Property>
      <Property :id="host.id" type="global" name="hostName">Host Name</Property>
      <Property :id="host.id" type="global" name="id">ID</Property>
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
        <div v-if="host.id.startsWith('sim-')">
          <span class="simulated-tag">Simulated</span>
          <Info hide-label>
            <template class="simulated-tag" #title>Simulated Host</template>
            <template #desc>This is a simulated host, not a real machine ! </template>
          </Info>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import Icon from './Common/Icon/Icon.vue';
import Info from './Common/Info/Info.vue';
import Property from './Common/Property/Property.vue';

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
.simulated-tag {
  background-color: #c6c6ff;
  border-radius: 0.3rem;
  padding: 0.1rem 0.5rem;
  margin-top: 0.5rem;
  color: var(--c-black);
}
</style>
