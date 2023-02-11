import { createStore } from 'vuex';

import preferences from '@/store/preferences';
import bridge from '@/store/bridge';

export default createStore({
  modules: {
    preferences,
    bridge,
  },
});
