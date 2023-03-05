<template>
  <main>
    <div class="static">
      <HostDetails class="host-section" :host="host" />
      <HostConfig class="host-section" :host="host" />
    </div>
    <Terminals :host="host" />
  </main>
</template>

<script setup>
import HostConfig from '@/components/HostConfig.vue';
import HostDetails from '@/components/HostDetails.vue';
import Terminals from '@/components/Terminals.vue';
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useStore } from 'vuex';

const route = useRoute();
const store = useStore();

const host = computed(() => store.getters.getHostById(route.params.hostid));
</script>

<style scoped>
main {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

section:not(:first-child) {
  margin-top: 1rem;
  border-top: 1px solid var(--c-border-soft);
}
.host-section:deep(.host-content) {
  padding: 0 1rem;
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

@media (min-width: 768px) {
}
@media (min-width: 1024px) {
  main .static {
    flex: 1;
    display: flex;
    flex-direction: row-reverse;
    justify-content: center;
  }
  main .static .host-section:first-child {
    flex: 0.4;
  }
  main .static .host-section:last-child {
    flex: 0.6;
  }
  section:not(:first-child) {
    margin-top: 0;
    border-top: none;
  }
  section:last-child {
    border-right: 1px solid var(--c-border);
  }
}
</style>
