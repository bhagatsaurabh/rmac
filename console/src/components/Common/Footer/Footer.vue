<template>
  <footer class="footer">
    <pre>&copy; 2022-present | Saurabh Bhagat</pre>
  </footer>
</template>

<script setup></script>

<style scoped>
footer.footer {
  text-align: center;
  padding: 1rem 0rem;
  border-top: 1px solid var(--c-border);
  color: var(--c-text-soft) !important;
  transition: var(--theme-color-transition), var(--theme-border-transition);
}
.footer pre {
  color: var(--c-text-mute);
}
</style>
