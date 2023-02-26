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
      <span class="filter">
        <Button
          @click="() => (open = !open)"
          class="filter-sort-button"
          icon="filter"
          :complementary="false"
        />
      </span>
    </div>
    <div :class="{ options: true, open }">
      <div class="options-category">
        <h2>Sort by</h2>
        <section>
          <p>Name</p>
          <hr />
          <div>
            <input
              type="radio"
              :value="true"
              name="sort-name"
              id="sort-name-ascending"
              v-model="config.sort.name"
              @input="changeHandler"
            />
            <label for="sort-name-ascending">A-Z</label>
          </div>
          <div>
            <input
              type="radio"
              :value="false"
              name="sort-name"
              id="sort-name-descending"
              v-model="config.sort.name"
              @input="changeHandler"
            />
            <label for="sort-name-descending">Z-A</label>
          </div>
        </section>
        <section>
          <p>Connection</p>
          <hr />
          <div>
            <input
              type="radio"
              value="online"
              name="sort-connection"
              id="sort-connection-online"
              v-model="config.sort.connection"
              @input="changeHandler"
            />
            <label for="sort-connection-online">Online</label>
          </div>
          <div>
            <input
              type="radio"
              value="offline"
              name="sort-connection"
              id="sort-connection-offline"
              v-model="config.sort.connection"
              @input="changeHandler"
            />
            <label for="sort-connection-offline">Offline</label>
          </div>
        </section>
        <section>
          <p>Registration</p>
          <hr />
          <div>
            <input
              type="radio"
              value="registered"
              name="sort-registration"
              id="sort-registration-registered"
              v-model="config.sort.registration"
              @input="changeHandler"
            />
            <label for="sort-registration-registered">Registered</label>
          </div>
          <div>
            <input
              type="radio"
              value="unknown"
              name="sort-registration"
              id="sort-registration-unknown"
              v-model="config.sort.registration"
              @input="changeHandler"
            />
            <label for="sort-registration-unknown">Unknown</label>
          </div>
        </section>
      </div>
      <div class="options-category">
        <h2>Filter by</h2>
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
import { watch, ref } from 'vue';
import Backdrop from './Backdrop.vue';
import Button from './Button.vue';
import Icon from './Icon.vue';

const config = ref({
  name: '',
  filter: { connection: [], registration: [] },
  sort: { type: null, order: true },
});

const emit = defineEmits(['query']);
const open = ref(false);

const changeHandler = () => {
  config.value = { ...config.value };
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
  padding: 1rem 1rem;
  position: sticky;
  box-shadow: 0 0 5px 0 var(--c-shadow);
  top: 4rem;
  background-color: var(--c-background-transparent);
  z-index: 9;
  transition: var(--theme-color-transition), var(--theme-bg-transition);
}
.controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}
.search input:focus {
  outline: none;
  box-shadow: 3px 3px 10px 0px var(--c-shadow);
}
.filter-sort-button {
  padding: 0.5rem;
  border-radius: 0;
  display: inline-block;
  margin-left: 0.5rem;
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

@media (min-width: 768px) {
}
@media (min-width: 1024px) {
  .controls {
    justify-content: end;
  }
}
</style>
