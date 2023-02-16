import { createRouter, createWebHistory } from 'vue-router';

import Launch from '@/views/Launch.vue';
import Dashboard from '@/views/Dashboard.vue';
import Host from '@/views/Host.vue';
import store from '@/store';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'launch',
      component: Launch,
      meta: {
        title: 'RMAC',
      },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: Dashboard,
      meta: {
        title: 'RMAC | Dashboard',
      },
      beforeEnter: connectionGuard,
    },
    {
      path: '/host/:hostid',
      name: 'host',
      component: Host,
      meta: {
        title: 'RMAC | Host',
      },
      beforeEnter: connectionGuard,
    },
  ],
});

const connectionGuard = (_to, _from, next) => {
  if (!store.state.bridge.connected) {
    return next('/');
  }
  return next();
};

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title;
  next();
});

export default router;
