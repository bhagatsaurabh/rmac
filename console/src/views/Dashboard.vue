<template>
  <main>
    <SearchFilter @query="queryHandler" />
    <HostList class="hosts" :hosts="hosts" />
  </main>
</template>

<script setup>
import SearchFilter from '@/components/Common/SearchFilter.vue';
import HostList from '@/components/HostList.vue';
import { computed } from 'vue';
import { useStore } from 'vuex';

const store = useStore();

const hosts = computed(() => store.state.hosts.hosts);

const queryHandler = (config) => {
  console.log(config);
};
</script>

<style scoped>
.hosts {
  margin-top: 4rem;
}
</style>
