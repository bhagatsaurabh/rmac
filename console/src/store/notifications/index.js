import { mutationKeys, themes } from '@/store/constants';

const state = () => ({
  data: [],
});

const mutations = {
  [mutationKeys.PUSH_NOTIFICATION]: (state, notification) => {
    const updatedNotifications = [...state.data];
    updatedNotifications.push(notification);
    state.data = updatedNotifications;
  },
  [mutationKeys.SET_READ_ALL]: (state) => {
    const updatedNotifications = [...state.data];
    updatedNotifications.forEach((notification) => (notification.read = true));
    state.data = updatedNotifications;
  },
};

const actions = {
  pushNotification({ commit }, notification) {
    commit(mutationKeys.PUSH_NOTIFICATION, notification);
  },
  readAllNotifications({ commit }) {
    commit(mutationKeys.SET_READ_ALL);
  },
};

const getters = {};

export default {
  state,
  mutations,
  actions,
  getters,
};
