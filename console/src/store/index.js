import { createStore } from 'vuex';

import preferences from '@/store/preferences';
import bridge from '@/store/bridge';
import notifications from './notifications';
import hosts from './hosts';

export default createStore({
  modules: {
    preferences,
    bridge,
    notifications,
    hosts,
  },
});
