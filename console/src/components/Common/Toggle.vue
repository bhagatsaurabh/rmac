<template>
  <button @click="clickHandler" v-bind="$attrs" class="switch">
    <input ref="inputElement" :id="id" type="checkbox" v-model="value" tabindex="-1" />
    <span class="slider"></span>
  </button>
  <label class="ml-0p5" :for="id"><slot></slot></label>
</template>

<script setup>
import { computed, ref, watch } from 'vue';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  id: {
    type: String,
    default: null,
  },
});
const emit = defineEmits(['update:modelValue', 'change']);

const inputElement = ref(null);

const value = computed({
  get() {
    return props.modelValue;
  },
  set(value) {
    emit('update:modelValue', value);
  },
});

const clickHandler = () => {
  value.value = !value.value;
};

defineExpose({ inputElement });

watch(value, () => {
  emit('change');
});
</script>

<style scoped>
.switch {
  position: relative;
  display: inline-block;
  width: 2.5rem;
  height: 1.2rem;
  background-color: unset;
  border: none;
  padding: 0;
  line-height: 1.5rem;
}
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 1rem;
}
.slider:before {
  position: absolute;
  content: '';
  height: 0.85rem;
  width: 0.85rem;
  left: 0.2rem;
  bottom: 0.175rem;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #2196f3;
}
input:focus + .slider {
  box-shadow: 0 0 1px #2196f3;
}
input:checked + .slider:before {
  transform: translateX(1.25rem);
}
</style>
