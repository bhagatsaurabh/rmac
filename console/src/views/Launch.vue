<template>
  <header>
    <Logo alt="RMAC logo" :config="{ maxHeight: '10rem' }" name="rmac-logo-combined" />

    <div class="wrapper">
      <nav>
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/">About</RouterLink>
      </nav>
    </div>
  </header>
</template>

<script setup>
import Logo from '@/components/Common/Logo.vue';
</script>

<style scoped></style>
