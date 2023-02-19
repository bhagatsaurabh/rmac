<template>
  <main class="launch">
    <div class="launch-left">
      <section class="banner">
        <Logo
          class="banner-logo"
          alt="RMAC logo"
          :config="{ maxHeight: '10rem' }"
          name="rmac-logo-combined"
          animated
        />
        <h1><pre>C o n s o l e</pre></h1>
      </section>
      <section class="links">
        <ExternalLink to="https://github.com/saurabh-prosoft/rmac">
          <template #prefix>
            <Icon alt="GitHub icon" name="icons/github" adaptive></Icon>
          </template>
          GitHub
        </ExternalLink>
        <ExternalLink to="https://github.com/saurabh-prosoft/rmac">
          <template #prefix>
            <Icon alt="Help icon" name="icons/help" adaptive></Icon>
          </template>
          Help
        </ExternalLink>
      </section>
    </div>
    <div class="launch-right">
      <section class="controls">
        <Button :busy="isConnecting" @click="handleConnect" icon="right-arrow" icon-right async>
          <span>Connect</span>
        </Button>
        <span class="launch-status">
          {{ statusMsg }}
        </span>
      </section>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';

import Button from '@/components/Common/Button.vue';
import ExternalLink from '@/components/Common/ExternalLink.vue';
import Icon from '@/components/Common/Icon.vue';
import Logo from '@/components/Common/Logo.vue';

const store = useStore();
const statusMsg = computed(() => store.state.bridge.statusMsg);
const isBridgeConnected = computed(() => store.state.bridge.connected);

const router = useRouter();

const isConnecting = ref(false);
const handleConnect = async () => {
  isConnecting.value = true;
  await store.dispatch('connectToBridge');
  isConnecting.value = false;
};

watch(isBridgeConnected, (newVal, oldVal) => {
  if (newVal !== oldVal && newVal) {
    router.push({ name: 'dashboard' });
  }
});

onMounted(async () => {
  if (isBridgeConnected.value) {
    await store.dispatch('disconnectFromBridge');
  }
});
</script>

<style scoped>
.launch {
  display: flex;
  justify-content: center;
  flex-direction: column;
  text-align: center;
}
.launch section {
  margin-bottom: 1.5rem;
}
.launch .links a {
  margin: 0.5rem 0.5rem;
}
.launch h1 pre {
  font-family: Arial, Helvetica, sans-serif;
  color: var(--c-text-mute);
}
.launch .banner {
  margin-bottom: 2rem !important;
}
.launch .banner-logo {
  margin: 0 auto;
}
.controls button {
  font-size: 1.1rem;
  margin: auto;
}

.launch-status {
  position: absolute;
  transform: translateX(-50%);
  margin-top: 0.5rem;
}

@media (min-width: 768px) {
}

@media (min-width: 1024px) {
  .launch {
    flex-direction: row;
    align-items: center;
  }
  .launch-left {
    margin-right: 6rem;
  }
  .launch-left::after {
    content: '';
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    right: -3rem;
    height: 60%;
    width: 1px;
    box-shadow: 0 0 5px 0 var(--c-shadow-soft);
    opacity: 0.6;
    border-right: 1px solid var(--c-border-soft);
  }
  .launch section:last-of-type {
    margin-bottom: 0;
  }
}
</style>
