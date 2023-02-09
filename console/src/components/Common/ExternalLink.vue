<template>
  <a :href="to" target="_blank" class="external-link">
    <button tabindex="-1">
      <slot name="prefix"></slot>
      <slot></slot>
      <Icon
        class="external-link-icon"
        alt="External link icon"
        name="icons/external-link"
        :size="0.8"
        :config="{ verticalAlign: 'super' }"
        adaptive
      />
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
  color: var(--c-text);
}
.external-link button {
  color: inherit;
  padding: 0.3rem 1rem;
  background-color: var(--c-background-mute);
  cursor: pointer;
  border: 1px solid var(--c-box-border);
  box-shadow: 2px 2px 5px 0 var(--c-shadow);
  transition: box-shadow var(--fx-transition-duration) linear,
    background-color var(--theme-transition-duration) linear,
    color var(--theme-transition-duration) linear, border var(--theme-transition-duration) linear;
}

.external-link button:active {
  box-shadow: 2px 2px 5px -2px var(--c-shadow), 3px 3px 5px 0 inset var(--c-shadow-soft);
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
