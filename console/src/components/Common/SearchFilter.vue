<template>
  <div class="search-filter-sort">
    <span class="search">
      <Icon alt="Search icon" name="icons/search" adaptive :size="1.2" />
      <input
        @input="inputChangeHandler"
        v-model="config.name"
        class="search-input"
        type="text"
        spellcheck="false"
        name="name"
        placeholder=""
      />
    </span>
    <span class="filter-sort">
      <span class="sort">
        <Button class="filter-sort-button" icon="sort" :complementary="false" />
      </span>
      <span class="filter">
        <Button class="filter-sort-button" icon="filter" :complementary="false" />
      </span>
    </span>
  </div>
</template>

<script setup>
import { watch } from 'vue';
import { ref } from 'vue';
import Button from './Button.vue';
import Icon from './Icon.vue';

const config = ref({
  name: '',
  filter: { connection: null, registration: null },
  sort: { connection: null, registration: null },
});

const emit = defineEmits(['query']);

const inputChangeHandler = () => {
  config.value = { ...config.value };
};

watch(config, () => {
  emit('query', config.value);
});
</script>

<style scoped>
.search-filter-sort {
  display: flex;
  width: 100%;
  padding: 1rem 1rem;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  box-shadow: 0 0 5px 0 var(--c-shadow);
  top: 4rem;
  background-color: var(--c-background);
  z-index: 9;
}
.search {
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
  padding: 0.3rem 0.3rem 0.3rem 2rem;
  border-radius: 4rem;
  border: 1px solid var(--c-border-soft);
  box-shadow: 3px 3px 10px -2px var(--c-shadow);
  transition: box-shadow var(--fx-transition-duration) linear;
  font-size: 1.2rem;
  color: var(--c-text-soft);
  max-width: 100%;
}
.search input:focus {
  outline: none;
  box-shadow: 3px 3px 10px 0px var(--c-shadow);
}
.filter-sort {
}
.filter-sort-button {
  padding: 0.5rem;
  border-radius: 0;
  display: inline-block;
  margin-left: 0.5rem;
}

@media (min-width: 768px) {
}
@media (min-width: 1024px) {
  .search-filter-sort {
    justify-content: end;
  }
}
</style>
