<template>
  <span class="external-link">
    <slot name="prefix"></slot>
    <a :href="to" target="_blank"><slot></slot></a>
    <Icon alt="External link icon" name="icons/external-link" adaptive></Icon>
  </span>
</template>

<script setup>
import Icon from './Icon.vue';

defineProps({
  to: {
    type: String,
    required: true,
  },
});
</script>

<style scoped>
.external-link {
}

.external-link a {
  text-decoration: none;
  color: rgb(94, 94, 94);
  font-weight: bold;
}
</style>
