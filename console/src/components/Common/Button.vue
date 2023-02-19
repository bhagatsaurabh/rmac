<template>
  <button
    :class="{
      control: true,
      circular,
      complementary,
    }"
    :disabled="disabled || busy"
  >
    <Icon
      class="mr-0p5"
      v-if="icon && iconLeft"
      v-hide="busy"
      :alt="`${icon} icon`"
      :name="`icons/${icon}`"
      adaptive
      invert
    />
    <Icon
      v-if="icon && !iconLeft && !iconRight"
      v-hide="busy"
      :alt="`${icon} icon`"
      :name="`icons/${icon}`"
      adaptive
      :invert="complementary"
    />
    <span v-hide="busy">
      <slot></slot>
    </span>
    <Icon
      class="ml-0p5"
      v-if="icon && iconRight"
      v-hide="busy"
      :alt="`${icon} icon`"
      :name="`icons/${icon}`"
      adaptive
      invert
    />
    <Spinner v-if="async" v-hide="!busy" :size="1.5" invert />
  </button>
</template>

<script setup>
import Icon from './Icon.vue';
import Spinner from './Spinner.vue';

defineProps({
  icon: {
    type: String,
    default: null,
  },
  iconLeft: {
    type: Boolean,
    default: false,
  },
  iconRight: {
    type: Boolean,
    default: false,
  },
  circular: {
    type: Boolean,
    default: false,
  },
  complementary: {
    type: Boolean,
    default: true,
  },
  async: {
    type: Boolean,
    default: false,
  },
  busy: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
.control {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: var(--c-text);
  border: 1px solid var(--c-box-border);
  padding: 0.5rem 1rem;
  background-color: var(--c-background);
  border-radius: 0.5rem;
  transition: var(--theme-bg-transition), var(--theme-color-transition),
    var(--theme-border-transition), box-shadow var(--fx-transition-duration) linear,
    opacity var(--fx-transition-duration) linear;
  box-shadow: 0 0 10px 0 var(--c-shadow);
}
.control.circular {
  padding: 0.5rem;
  border-radius: 999px;
}
.control.complementary {
  background-color: var(--c-text-soft);
  color: var(--c-background);
}
.control:active {
  box-shadow: 0 0 5px 0 var(--c-shadow);
}
@media (hover: hover) {
  .control.complementary:hover {
    background-color: var(--c-text-mute);
  }
}
.control:disabled {
  opacity: 0.6;
}

.control:deep(span) {
  display: inline-flex;
  transition: opacity 0.2s linear;
}
.control:deep(.spinner) {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}
</style>
