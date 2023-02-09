import { createRouter, createWebHistory } from 'vue-router';

import Launch from '@/views/Launch.vue';
import Dashboard from '@/views/Dashboard.vue';
import Host from '@/views/Host.vue';

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
    },
    {
      path: '/host/:hostid',
      name: 'host',
      component: Host,
      meta: {
        title: 'RMAC | Host',
      },
    },
  ],
});

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title;
  next();
});

export default router;
