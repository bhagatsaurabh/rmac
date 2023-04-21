<template>
  <main>
    <SearchFilter @query="queryHandler" />
    <HostList class="hosts" :hosts="filteredHosts" />
  </main>
</template>

<script setup>
import SearchFilter from '@/components/Common/SearchFilter/SearchFilter.vue';
import HostList from '@/components/HostList/HostList.vue';
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
    config: {
      name: '',
      filter: { connection: [], registration: [] },
      sort: { type: 'name', order: true },
      simulated: true,
    },
    hosts: hosts.value,
  });
});
</script>

<style scoped></style>
