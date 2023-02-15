<template>
  <Button v-bind="$attrs" @click="isOpen = !isOpen" icon="bell" circular :complementary="false">
    <span v-if="count !== 0" class="notifications-count">{{ count }}</span>
  </Button>
  <aside :class="{ notifications: true, open: isOpen }">
    <header>
      <h1>Notifications</h1>
      <Icon @click="router.back()" alt="Close icon" name="icons/cancel" adaptive :size="2" />
    </header>
    <section>
      <Notification v-for="ntfcn in notifications" :data="ntfcn" />
      <div v-if="notifications.length === 0" class="empty">
        <Icon alt="Empty icon" name="icons/empty" adaptive :size="2" />
        Empty
      </div>
    </section>
  </aside>
  <Transition name="toast">
    <Toast @click="handleToastClick" v-if="toastNotification" :data="toastNotification" />
  </Transition>
</template>

<script setup>
import { computed, ref, watch, onBeforeUnmount } from 'vue';
import { useStore } from 'vuex';
import Icon from './Icon.vue';
import { useRouter } from 'vue-router';

import Notification from './Notification.vue';
import Button from './Button.vue';
import bus from '@/event';
import Toast from './Toast.vue';

const store = useStore();
const notifications = computed(() => store.state.notifications.data);
const count = computed(() => notifications.value?.filter((ntfcn) => !ntfcn.read).length ?? 0);
// const count = computed(() => 5);

const isOpen = ref(false);
const toastNotification = ref(null);
const timeoutHandle = ref(-1);

const handleToastClick = () => {
  clearTimeout(timeoutHandle.value);
  toastNotification.value = null;
  isOpen.value = !isOpen.value;
};

bus.on('notify', async (data) => {
  await store.dispatch('pushNotification', data);
  if (!isOpen.value) {
    toastNotification.value = data;
    clearTimeout(timeoutHandle.value);
    timeoutHandle.value = setTimeout(() => (toastNotification.value = null), 4000);
  }
});

const router = useRouter();
let unregisterGuard = () => {};
watch(
  isOpen,
  async (newVal, oldVal) => {
    if (oldVal !== newVal && newVal) {
      await router.push({ hash: '#notifications' });
      await store.dispatch('readAllNotifications');

      unregisterGuard = router.beforeEach((_to, from, next) => {
        if (from.hash.startsWith('#notifications')) {
          isOpen.value = false;
        }
        unregisterGuard();
        next();
      });
    }
  },
  { immediate: true }
);

onBeforeUnmount(unregisterGuard);
</script>

<style scoped>
.notifications {
  position: fixed;
  top: 0;
  right: calc(-100vw - 10px);
  z-index: 100;
  width: 100vw;
  height: 100vh;
  transition: right var(--fx-transition-duration-slow) ease-out;
  background-color: var(--c-background);
  box-shadow: 0 0 10px 0 var(--c-shadow);
}
.notifications.open {
  right: 0;
}
.notifications header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--c-border);
  padding: 1rem;
}
.notifications header .icon-container {
  font-size: 0;
}
.notifications-count {
  position: absolute;
  top: -1.5rem;
  right: -0.8rem;
  padding: 0.2rem;
  border-radius: 999px;
  pointer-events: all;
  color: var(--c-background);
  background-color: var(--c-text);
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity var(--fx-transition-duration) ease,
    transform var(--fx-transition-duration) ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(1rem);
}

.empty {
  text-align: center;
  margin-top: 2rem;
  opacity: 0.7;
  font-size: 1.6rem;
}
.empty .icon-container {
  margin-right: 0.5rem;
}
</style>
