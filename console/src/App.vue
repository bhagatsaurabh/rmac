<script setup>
import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
</script>

<template>
  <header>
    <img alt="Vue logo" class="logo" src="@/assets/rmac-logo-combined.png" />

    <div class="wrapper">
      <HelloWorld msg="You did it!" />

      <nav>
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/about">About</RouterLink>
      </nav>
    </div>
  </header>

  <RouterView />
</template>

<script>

export default {
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
}
</script>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
  max-height: 9rem;
}

nav {
  width: 100%;
  font-size: 12px;
  text-align: center;
  margin-top: 2rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;

    padding: 1rem 0;
    margin-top: 1rem;
  }
}
</style>
