<template>
  <Button @click="isOpen != isOpen" icon="bell" />
  <aside :class="{ notifications: true, active: isOpen }">
    <header>
      <h1>Notifications</h1>
      <Icon alt="Close icon" name="icons/cancel" adaptive="" />
    </header>
    <section>
      <Notification v-for="ntfcn in notifications" :data="ntfcn" />
    </section>
  </aside>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import Icon from './Icon.vue';
import Notification from './Notification.vue';
import Button from './Button.vue';

const store = useStore();
const notifications = computed(() => store.state.notifications.data);

const isOpen = ref(false);
</script>

<style scoped>
.notifications {
  position: fixed;
  top: 0;
  right: -100vw;
  z-index: 100;
  width: 100vw;
  height: 100vh;
  transition: right var(--fx-transition-duration) ease-out;
}
.notifications.active {
  right: 0;
}
.notifications header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
