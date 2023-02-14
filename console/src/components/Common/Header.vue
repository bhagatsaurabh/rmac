<template>
  <header class="header">
    <div class="left">
      <slot name="left"></slot>
    </div>
    <div class="right">
      <slot name="right"></slot>
    </div>
  </header>
</template>

<script setup></script>

<style scoped>
header.header {
  display: flex;
  width: 100vw;
  height: 4rem;
  position: sticky;
  top: -1rem;
  z-index: 10;
  padding: 0 0.5rem;
}
header.header::before,
header.header::after {
  content: '';
  display: block;
  height: 1rem;
  position: sticky;
}
header::before {
  top: 3rem;
  box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.5);
}
header::after {
  background: linear-gradient(
    white 10%,
    rgba(255, 255, 255, 0.8) 50%,
    rgba(255, 255, 255, 0.4) 70%,
    transparent
  );
  top: 0;
  z-index: 11;
}

header.header .left,
header.header .right {
  flex: 1;
  display: flex;
  align-items: center;
}
header.header .left {
  justify-content: start;
}
header.header .right {
  justify-content: end;
}
</style>
