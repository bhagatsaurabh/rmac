<template>
  <section>
    <header>
      <Icon alt="Config icon" name="icons/config" adaptive :size="1.4" />
      <h2>Config</h2>
    </header>
    <div class="host-content">
      <div v-if="loading" class="config-spinner">
        <Spinner :size="2" />
      </div>
      <div class="no-config" v-else-if="!host.config">
        <Icon alt="Offline icon" name="icons/host-offline" :size="2" adaptive />
        <h2>Host is offline</h2>
      </div>
      <div v-else class="config"></div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useStore } from 'vuex';
import Icon from './Common/Icon.vue';
import Spinner from './Common/Spinner.vue';

const store = useStore();

const props = defineProps({
  host: {
    type: Object,
    required: true,
  },
});

const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  await store.dispatch('fetchConfig', props.host.id);
  loading.value = false;
});
</script>

<style scoped>
.no-config,
.config-spinner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}
.no-config:deep(.icon-container) {
  opacity: 0.6;
  font-size: 0;
  margin-right: 0.5rem;
}
</style>
