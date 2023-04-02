<template>
  <nav class="terminals-nav">
    <ul ref="navList">
      <li class="add"><button @click="emit('add')">+</button></li>
      <li v-for="(id, index) in terminals" :class="{ active: id === active }">
        <button @click="emit('select', id)">{{ index + 1 }}</button>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { onUpdated, ref } from 'vue';

defineProps({
  terminals: {
    type: Array,
    required: true,
  },
  active: {
    type: String,
    required: false,
  },
});

const emit = defineEmits(['select', 'add']);
const navList = ref(null);

onUpdated(() => {
  document.querySelector('.terminals-nav ul li.active')?.scrollIntoView({ block: 'nearest' });
});
</script>

<style scoped>
.terminals-nav {
  width: 2rem;
  height: 100%;
  overflow-y: auto;
  border-left: 1px solid var(--c-border);
}
.terminals-nav ul {
  list-style: none;
  padding: 0;
}
.terminals-nav ul li {
  display: block;
  padding: 0;
}
.terminals-nav ul li button {
  width: 100%;
  height: 100%;
  border: none;
  background-color: var(--c-background);
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
  font-weight: bold;
  cursor: pointer;
  transition: background-color var(--fx-transition-duration) linear,
    color var(--fx-transition-duration) linear;
}
.terminals-nav ul li.active {
  box-shadow: 0 0 10px 0 var(--c-shadow);
  z-index: 1;
}
.terminals-nav ul li.active button {
  background-color: var(--c-text);
  color: var(--c-background);
}
.terminals-nav ul li.add button {
  font-size: 1.3rem;
  padding: 0.2rem;
  border-bottom: 1px solid var(--c-border);
  transition: background-color var(--fx-transition-duration) linear;
}

@media (hover: hover) {
  .terminals-nav ul li.add button:hover {
    background-color: var(--c-background-soft);
  }
}
@media (min-width: 1024px) {
  .terminals-nav {
    width: 3rem;
    padding-right: 1rem;
  }
}
</style>
