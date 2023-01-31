<template>
  <ThemeSelector />
  <RouterView />
</template>

<script setup>
import ThemeSelector from '@/components/Common/ThemeSelector.vue';
import { useStore } from 'vuex';
import { onBeforeUnmount, computed } from 'vue';
import { themes } from '@/store/constants';

const store = useStore();
store.dispatch('loadPreferences');
const currTheme = computed(() => store.state.preferences.theme);

const mediaChangeHandler = async (e) =>
  e.matches &&
  currTheme.value === themes.SYSTEM &&
  (await store.dispatch('setTheme', themes.SYSTEM));

const mediaHighCntrst = window.matchMedia('(prefers-contrast: more)');
mediaHighCntrst.addEventListener('change', mediaChangeHandler);

const mediaDark = window.matchMedia('(prefers-color-scheme: dark)');
mediaDark.addEventListener('change', mediaChangeHandler);

const mediaLight = window.matchMedia('(prefers-color-scheme: light)');
mediaLight.addEventListener('change', mediaChangeHandler);

onBeforeUnmount(() => {
  mediaHighCntrst.removeEventListener('change', mediaChangeHandler);
  mediaDark.removeEventListener('change', mediaChangeHandler);
  mediaLight.removeEventListener('change', mediaChangeHandler);
});
</script>

<script>
/* export default {
  mounted() {
    let pingTimer = -1;

    let socket;
    if (!import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL) {
      const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
      socket = new WebSocket(`${protocol}//${window.location.host}`);
    } else {
      socket = new WebSocket(import.meta.env.VITE_RMAC_BRIDGE_SERVER_URL);
    }

    socket.onclose = () => {
      console.log('Disconnected from bridging server');
      clearTimeout(pingTimer);
    };
    socket.onerror = (err) => {
      console.log(err);
    };

    socket.onopen = () => {
      console.log('Connected to bridging server');
      heartbeat();
      socket.send(JSON.stringify({ event: 'identity', type: 'console' }));
      setTimeout(() => { socket.send(JSON.stringify({ event: 'config', type: 'console' })) }, 5000);
    };
    socket.onmessage = (messageEvent) => {
      if (messageEvent.data === '?') {
        heartbeat();
        socket.send('?');
        return;
      }

      const message = JSON.parse(messageEvent.data);
      if (message.event === 'health') {
        console.log(message);
      } else if (message.event === 'config') {
        console.log(message);
      }
    };

    const heartbeat = () => {
      clearTimeout(pingTimer);
      pingTimer = setTimeout(() => {
        socket.close();
      }, 30000 + 1000);
    }
  }
} */
</script>

<style scoped></style>
