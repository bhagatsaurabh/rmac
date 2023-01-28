import { createApp } from 'vue';
import vHide from '@/directives/hide';

import App from '@/App.vue';
import router from '@/router';
import store from '@/store';

import './assets/main.css';

const app = createApp(App);

app.use(router);
app.use(store);
app.directive('hide', vHide);

app.mount('#app');
