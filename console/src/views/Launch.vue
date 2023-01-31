<template>
  <div class="launch">
    <div class="banner">
      <Logo alt="RMAC logo" :config="{ maxHeight: '10rem' }" name="rmac-logo-combined" animated />
      <h1>Console</h1>
    </div>
    <hr />
    <div class="input">
      <input type="text" />
    </div>
  </div>
</template>

<script setup>
import Logo from '@/components/Common/Logo.vue';
</script>

<style scoped>
.launch {
  display: flex;
  justify-content: center;
  flex-direction: column;
  height: 100vh;
  text-align: center;
}
.launch .banner {
}
.launch hr {
  margin: 1rem 30vw;
}

.launch .input {
}

@media (min-width: 768px) {
}

@media (min-width: 1024px) {
}
</style>
