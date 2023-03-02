<template>
  <Backdrop class="filter-overlay" @dismiss="() => (open = !open)" :show="open" />
  <div v-bind="$attrs" class="search-filter-sort">
    <div class="controls">
      <span class="search">
        <Icon alt="Search icon" name="icons/search" adaptive :size="1.2" />
        <input
          @input="changeHandler"
          v-model="config.name"
          class="search-input"
          type="text"
          spellcheck="false"
          name="name"
          placeholder=""
        />
      </span>
      <span>
        <Button
          class="control"
          @click="disconnect"
          icon="disconnect"
          icon-left
          :complementary="false"
          async
        >
          Disconnect
        </Button>
      </span>
      <span>
        <Button
          class="control"
          :busy="isRefreshing"
          @click="refresh"
          icon="refresh"
          icon-left
          :complementary="false"
          async
        >
          Refresh
        </Button>
      </span>
      <span class="filter">
        <Button
          @click="() => (open = !open)"
          class="control"
          icon="filter"
          icon-left
          :complementary="false"
        >
          Filter
        </Button>
      </span>
    </div>
    <div :class="{ options: true, open }">
      <div class="options-category">
        <h2>
          Sort by
          <Button @click="clearSort" icon="cancel" icon-left :complementary="false">Clear</Button>
        </h2>
        <button
          :ref="(el) => (sortOptions[0] = el)"
          @click="() => sortChangeHandler(0, 'name')"
          class="sort-option"
        >
          <Icon alt="Check icon" name="icons/check" adaptive :size="1.3" />
          <span>Name</span>
          <p v-if="config.sort.type === 'name'">{{ config.sort.order ? 'A 🡒 Z' : 'Z 🡒 A' }}</p>
        </button>
        <button
          :ref="(el) => (sortOptions[1] = el)"
          @click="() => sortChangeHandler(1, 'connection')"
          class="sort-option"
        >
          <Icon alt="Check icon" name="icons/check" adaptive :size="1.3" />
          <span>Connection</span>
          <p v-if="config.sort.type === 'connection'">
            {{ config.sort.order ? 'Online first' : 'Offline first' }}
          </p>
        </button>
        <button
          :ref="(el) => (sortOptions[2] = el)"
          @click="() => sortChangeHandler(2, 'registration')"
          class="sort-option"
        >
          <Icon alt="Check icon" name="icons/check" adaptive :size="1.3" />
          <span>Registration</span>
          <p v-if="config.sort.type === 'registration'">
            {{ config.sort.order ? 'Registered first' : 'Unknown first' }}
          </p>
        </button>
      </div>
      <div class="options-category">
        <h2>
          Filter by
          <Button @click="clearFilter" icon="cancel" icon-left :complementary="false">Clear</Button>
        </h2>
        <section>
          <p>Connection</p>
          <hr />
          <div>
            <input
              type="checkbox"
              value="online"
              id="filter-connection-online"
              v-model="config.filter.connection"
              @change="changeHandler"
            />
            <label for="filter-connection-online">Online</label>
          </div>
          <div>
            <input
              type="checkbox"
              value="offline"
              id="filter-connection-offline"
              v-model="config.filter.connection"
              @change="changeHandler"
            />
            <label for="filter-connection-offline">Offline</label>
          </div>
        </section>
        <section>
          <p>Registration</p>
          <hr />
          <div>
            <input
              type="checkbox"
              value="registered"
              name="filter-registration"
              id="filter-registration-registered"
              v-model="config.filter.registration"
              @change="changeHandler"
            />
            <label for="filter-registration-registered">Registered</label>
          </div>
          <div>
            <input
              type="checkbox"
              value="unknown"
              name="filter-registration"
              id="filter-registration-unknown"
              v-model="config.filter.registration"
              @change="changeHandler"
            />
            <label for="filter-registration-unknown">Unknown</label>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import store from '@/store';
import { watch, ref } from 'vue';
import Backdrop from './Backdrop.vue';
import Button from './Button.vue';
import Icon from './Icon.vue';

const config = ref({
  name: '',
  filter: { connection: [], registration: [] },
  sort: { type: null, order: true },
});
const sortOptions = ref([]);
const isRefreshing = ref(false);

const emit = defineEmits(['query']);
const open = ref(false);

const changeHandler = () => {
  config.value = { ...config.value };
};
const sortChangeHandler = (idx, type) => {
  config.value.sort.type = type;
  if (sortOptions.value[idx].classList.contains('active')) {
    config.value.sort.order = !config.value.sort.order;
  } else {
    sortOptions.value.forEach((el) => el.classList.remove('active'));
    sortOptions.value[idx].classList.add('active');
  }
  changeHandler();
};
const clearSort = () => {
  sortOptions.value.forEach((el) => el.classList.remove('active'));
  config.value.sort = { type: null, order: true };
  changeHandler();
};
const clearFilter = () => {
  config.value.filter = { connection: [], registration: [] };
  changeHandler();
};
const refresh = async () => {
  isRefreshing.value = true;
  await store.dispatch('fetchHosts');
  isRefreshing.value = false;
  changeHandler();
};
const disconnect = () => {
  store.dispatch('disconnectFromBridge');
};

watch(config, () => {
  emit('query', config.value);
});
</script>

<style scoped>
.filter-overlay {
  z-index: 5;
}
.search-filter-sort {
  width: 100%;
  padding: 1rem;
  position: sticky;
  box-shadow: 0 0 5px 0 var(--c-shadow);
  top: 4rem;
  background-color: var(--c-background-transparent);
  z-index: 9;
  transition: var(--theme-color-transition), var(--theme-bg-transition);
}
.controls {
}
.search {
  align-self: start;
  max-width: 70%;
}
.search .icon-container {
  position: absolute;
  z-index: 1;
  margin-left: 0.6rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0;
}
.search input {
  padding: 0.3rem 0.3rem 0.3rem 2.5rem;
  border-radius: 4rem;
  border: 1px solid var(--c-border-soft);
  box-shadow: 3px 3px 10px -2px var(--c-shadow);
  transition: box-shadow var(--fx-transition-duration) linear, var(--theme-bg-transition),
    var(--theme-color-transition);
  font-size: 1.2rem;
  color: var(--c-text-soft);
  max-width: 100%;
  background-color: var(--c-background);
  margin-right: 0.5rem;
}
.search input:focus {
  outline: none;
  box-shadow: 3px 3px 10px 0px var(--c-shadow);
}
.control {
  padding: 0.5rem;
  border-radius: 0;
  display: inline-block;
  margin-top: 1rem;
}
.controls .control {
  margin-right: 0.5rem;
}
.options {
  transition: all var(--fx-transition-duration-slow) ease-out;
  max-height: 0rem;
  overflow: hidden;
  display: flex;
  gap: 1rem;
}
.options.open {
  max-height: 30rem;
  overflow: visible;
}
.options-category {
  flex: 1;
  margin-top: 1rem;
}
.options-category p {
  color: var(--c-text-mute);
  font-style: italic;
  margin: 1rem 0 0.5rem 0;
}
.options-category input {
  margin-right: 0.5rem;
}

.options-category section div {
  margin: 0.2rem 0;
}

.sort-option {
  padding: 0.5rem 0.5rem;
  border: 1px solid var(--c-border-soft);
  opacity: 0.8;
  transition: opacity var(--fx-transition-duration) linear;
  cursor: pointer;
  width: 100%;
  background-color: var(--c-background-soft);
}
.sort-option.active {
  opacity: 1;
}

.sort-option .icon-container {
  opacity: 0;
}

.sort-option.active .icon-container {
  opacity: 1;
}

.sort-option .icon-container {
  position: absolute;
  left: 0.7rem;
  top: 50%;
  transform: translateY(-50%);
  transition: opacity var(--fx-transition-duration) linear;
}

.sort-option:first-of-type {
  margin-top: 1.5rem;
}
.sort-option:not(:last-child) {
  margin-bottom: 0.5rem;
}

.sort-option p {
  margin: 0 0;
}

.options-category h2 button {
  display: inline-block;
  padding: 0.5rem;
}
.options-category h2 {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (hover: hover) {
  .sort-option:hover {
    opacity: 1;
  }
}
@media (min-width: 768px) {
  .search-filter-sort {
    padding: 0 1rem 1rem 1rem;
  }
  .filter-overlay {
    background-color: transparent;
    backdrop-filter: unset;
  }
  .options {
    max-height: unset;
    position: absolute;
    overflow: unset;
    background-color: var(--c-background);
    box-shadow: 0px 11px 20px -7px var(--c-shadow);
    border: 1px solid var(--c-box-border);
    left: 50%;
    transform: translateX(-50%);
    top: 100%;
    padding: 0 1rem 1rem 1rem;
    width: 80%;
    visibility: hidden;
    pointer-events: none;
  }
  .options.open {
    max-height: unset;
    animation-name: drop-in;
    animation-duration: var(--fx-transition-duration-slow);
    animation-timing-function: ease-in-out;
    animation-fill-mode: both;
    pointer-events: all;
  }

  @keyframes drop-in {
    0% {
      visibility: hidden;
      opacity: 0;
    }
    100% {
      visibility: visible;
      opacity: 1;
    }
  }
}
@media (min-width: 1024px) {
  .search input {
    margin-right: 1rem;
    margin-top: unset;
  }
  .options {
    left: 0;
    transform: unset;
    width: min(60%, 35rem);
  }
}
</style>
