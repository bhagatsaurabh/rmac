<template>
  <RouterLink :to="`/host/${host.id}`">
    <div class="host">
      <div class="header">
        {{ host.clientName }}
      </div>
      <Icon alt="Host icon" name="icons/host" :size="3" adaptive />
      <div class="footer">
        <span :class="{ health: true, online: host.health, offline: !host.health }">
          {{ host.health ? 'Online' : 'Offline' }}
        </span>
        <span class="tag">{{ host.registered ? 'Registered' : 'Unknown' }}</span>
      </div>
    </div>
  </RouterLink>
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

<style scoped>
.host {
  color: var(--c-text);
  background-color: var(--c-background-soft);
  padding: 0.5rem 1rem;
  margin: 1rem;
  border: 1px solid var(--c-border);
  box-shadow: 0 0 10px 0px var(--c-shadow-soft);
  transition: box-shadow var(--fx-transition-duration) linear;
  text-align: center;
  width: 10rem;
}
.host .header {
  text-align: center;
  text-overflow: ellipsis;
  overflow: hidden;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}
.host .footer {
  display: flex;
  justify-content: space-between;
  margin-top: 0.5rem;
}

.health {
  font-size: 0.9rem;
}
.health::before {
  content: '';
  display: inline-block;
  width: 0.6rem;
  height: 0.6rem;
  border-radius: 0.2rem;
  border: 1px solid var(--c-text);
  margin-right: 0.5rem;
}
.health.online::before {
  background-color: green;
}
.health.offline::before {
  background-color: red;
}

@media (hover: hover) {
  .host {
    cursor: pointer;
  }
  .host:hover {
    box-shadow: 0 0 10px 0 var(--c-shadow);
  }
}
</style>
