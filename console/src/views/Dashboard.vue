<template>
  <main>
    <HostList :hosts="hosts" />
  </main>
</template>

<script setup>
import HostList from '@/components/HostList.vue';
import { computed } from 'vue';
import { useStore } from 'vuex';

const store = useStore();

const hosts = computed(() => store.state.hosts.hosts);
</script>

<style scoped></style>
