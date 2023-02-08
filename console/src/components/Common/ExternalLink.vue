<template>
  <a :href="to" target="_blank" class="external-link">
    <button tabindex="-1">
      <slot name="prefix"></slot>
      <slot></slot>
      <Icon
        class="external-link-icon"
        alt="External link icon"
        name="icons/external-link"
        :config="{ maxWidth: '.8rem', verticalAlign: 'super' }"
        adaptive
      ></Icon>
    </button>
  </a>
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
.external-link button {
  padding: 0.3rem 1rem;
  background-color: rgb(243 243 243);
  cursor: pointer;
  border: none;
  box-shadow: 2px 2px 5px 0 #8b8b8b;
  transition: box-shadow 0.1s linear;
}

.external-link button:active {
  box-shadow: 2px 2px 5px -2px #8b8b8b, 3px 3px 5px 0 inset lightgrey;
}

.external-link-icon {
  opacity: 0.5;
}

.external-link a {
  text-decoration: none;
  color: rgb(94, 94, 94);
  font-weight: bold;
}
</style>
