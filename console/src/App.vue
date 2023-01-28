<template>
    <RouterView />
</template>

<script setup>
import { useStore } from 'vuex';
import { onBeforeUnmount } from 'vue';
import { themes } from '@/store/constants';

const store = useStore();
const setSystemTheme = (theme) => store.dispatch('setSystemTheme', theme);
store.dispatch('loadPreferences');

const mediaChangeHandler = async (e, theme) => e.matches && (await setSystemTheme(theme));

const mediaHighContrast = window.matchMedia('(prefers-contrast: more)');
mediaHighContrast.addEventListener(
  'change',
  async (e) => await mediaChangeHandler(e, themes.HIGH_CONTRAST)
);

const mediaDark = window.matchMedia('(prefers-color-scheme: dark)');
mediaDark.addEventListener('change', async (e) => await mediaChangeHandler(e, themes.DARK));

const mediaLight = window.matchMedia('(prefers-color-scheme: light)');
mediaLight.addEventListener('change', async (e) => await mediaChangeHandler(e, themes.LIGHT));

onBeforeUnmount(() => {
  mediaHighContrast.removeEventListener('change', mediaChangeHandler);
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
