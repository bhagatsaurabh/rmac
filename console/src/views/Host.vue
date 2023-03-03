<template>
  <main>
    <HostDetails class="host-section" :host="host" />
    <HostConfig class="host-section" :host="host" />
    <Terminal class="host-section" :host="host" />
  </main>
</template>

<script setup>
import HostConfig from '@/components/HostConfig.vue';
import HostDetails from '@/components/HostDetails.vue';
import Terminal from '@/components/Terminal.vue';
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useStore } from 'vuex';

const route = useRoute();
const store = useStore();

const host = computed(() => store.getters.getHostById(route.params.hostid));
</script>

<style scoped>
section:not(:first-child) {
  margin-top: 1rem;
  border-top: 1px solid var(--c-border-soft);
}

.host-section:deep(header) {
  display: flex;
  align-items: center;
  position: sticky;
  top: 4rem;
  width: 100%;
  background-color: var(--c-background);
  border: 1px solid var(--c-box-border);
  padding: 0.5rem 1rem;
  box-shadow: 0 6px 6px -4px var(--c-shadow);
  z-index: 1;
  margin-bottom: 1rem;
}
.host-section:deep(header .icon-container) {
  font-size: 0;
  margin-right: 1rem;
}
.host-section:deep(header h2) {
  display: inline-block;
}
</style>
