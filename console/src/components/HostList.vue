<template>
  <h1 class="ml-1 mt-0p5">Hosts</h1>
  <section v-bind="$attrs" class="hosts-list">
    <HostCard v-for="host in hosts" :key="host.id" :host="host" />
    <div class="no-hosts" v-if="hosts.length === 0">
      <Icon alt="Empty icon" name="icons/empty" adaptive :size="3" />
      <p>
        {{ orgHosts.length === 0 ? 'No Hosts' : 'No Hosts found for specified filter criteria' }}
      </p>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue';
import { useStore } from 'vuex';
import Icon from './Common/Icon.vue';
import HostCard from './HostCard.vue';

const store = useStore();

const orgHosts = computed(() => store.state.hosts.hosts);

defineProps({
  hosts: {
    type: Object,
    required: true,
  },
});
</script>

<style scoped>
.hosts-list {
  display: flex;
  flex-wrap: wrap;
}

.hosts-list a {
  text-decoration: none;
}

.no-hosts {
  width: 100%;
  text-align: center;
  margin-top: 2rem;
  opacity: 0.8;
}
</style>
