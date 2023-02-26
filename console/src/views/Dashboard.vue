<template>
  <main>
    <SearchFilter @query="queryHandler" />
    <HostList class="hosts" :hosts="filteredHosts" />
  </main>
</template>

<script setup>
import SearchFilter from '@/components/Common/SearchFilter.vue';
import HostList from '@/components/HostList.vue';
import { computed, onMounted } from 'vue';
import { useStore } from 'vuex';

const store = useStore();

const hosts = computed(() => store.state.hosts.hosts);
const filteredHosts = computed(() => store.state.hosts.filteredHosts);

const queryHandler = (config) => {
  store.dispatch('filter', { config, hosts: hosts.value });
};

onMounted(() => {
  store.dispatch('filter', {
    config: { name: '', sort: {}, filter: { connection: [], registration: [] } },
    hosts: hosts.value,
  });
});
</script>

<style scoped></style>
