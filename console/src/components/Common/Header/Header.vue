<template>
  <header :class="{ header: true, drop: route.path !== '/' }">
    <div class="left">
      <slot name="left"></slot>
    </div>
    <div class="right">
      <slot name="right"></slot>
    </div>
  </header>
</template>

<script setup>
import { useRoute } from 'vue-router';

const route = useRoute();
</script>

<style scoped>
.header {
  display: flex;
  width: 100%;
  height: 4rem;
  top: 0;
  position: fixed;
  z-index: 10;
  background-color: var(--c-background);
  border-bottom: 1px solid var(--c-box-border);
}
.header.drop {
  box-shadow: 0 0 5px 0 var(--c-shadow);
}

header.header .left,
header.header .right {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 0 0.5rem;
}
header.header .left {
  justify-content: start;
}
header.header .right {
  justify-content: end;
}
</style>
