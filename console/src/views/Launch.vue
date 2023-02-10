<template>
  <main class="launch">
    <section class="banner">
      <Logo alt="RMAC logo" :config="{ maxHeight: '10rem' }" name="rmac-logo-combined" animated />
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
    <section class="input">
      <Info class="infocon">
        <template #title>Server URL</template>
        <template #desc>The RMAC Server through which all your hosts register.</template>
      </Info>
      <input type="text" name="rmac-server-url" placeholder="my-rmac-server.com" required />
    </section>
    <section class="controls">
      <Button :busy="isConnecting" @click="handleConnect" icon="right-arrow" icon-right>
        Connect
      </Button>
      <span class="launch-status">
        {{ statusMsg }}
      </span>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue';

import Button from '@/components/Common/Button.vue';
import ExternalLink from '@/components/Common/ExternalLink.vue';
import Icon from '@/components/Common/Icon.vue';
import Info from '@/components/Common/Info.vue';
import Logo from '@/components/Common/Logo.vue';

const isConnecting = ref(false);
const statusMsg = ref('');

const emit = defineEmits(['connected']);

let socket;
const handleFinish = (err) => {
  if (err) {
    
  }
  isConnecting.value = false;
};
const handleConnect = () => {
  isConnecting.value = true;

  if (!import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL) {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    socket = new WebSocket(`${protocol}//${window.location.host}`);
  } else {
    socket = new WebSocket(import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL);
  }

  socket.addEventListener('open', handleFinish);
  socket.addEventListener('close', handleFinish);
  socket.addEventListener('error', handleFinish);

  emit('connected', socket);
};

onBeforeUnmount(() => {
  socket?.removeEventListener('open', handleFinish);
  socket?.removeEventListener('close', handleFinish);
  socket?.removeEventListener('error', handleFinish);
});
</script>

<style scoped>
.launch {
  display: flex;
  justify-content: center;
  flex-direction: column;
  height: calc(100vh - 3.5rem - 1px);
  text-align: center;
}
.launch section:not(:last-child) {
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

.input label {
  display: block;
}
.input input {
  color: var(--c-text);
  width: 60vw;
  text-align: center;
  font-size: 1.1rem;
  padding: 0.3rem 1rem;
  border-radius: 999px;
  border: 1px solid var(--c-border);
  background-color: var(--c-background);
  margin-top: 0.5rem;
  box-shadow: 0 0 5px 1px var(--c-shadow-soft) inset;
  opacity: 0.8;
  transition: box-shadow var(--fx-transition-duration) linear,
    border var(--fx-transition-duration) linear, opacity var(--fx-transition-duration) linear,
    var(--theme-bg-transition);
}

.input input:focus {
  opacity: 1;
  outline: none;
  box-shadow: 0 0 0 0 var(--c-shadow-soft) inset, 4px 4px 10px -2px var(--c-shadow);
}

.input input:focus {
  outline: var(--c-border);
}
.infocon {
  display: inline-block;
}

.controls button {
  font-size: 1.1rem;
}

@media (min-width: 768px) {
}

@media (min-width: 1024px) {
}
</style>
