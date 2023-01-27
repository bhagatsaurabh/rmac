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
      component: Launch
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: Dashboard
    },
    {
      path: '/host/:hostid',
      name: 'host',
      component: Host
    }
  ]
})

export default router
