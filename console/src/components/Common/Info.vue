<template>
  <div class="infocon-container">
    <span>
      <slot name="title"></slot>
    </span>
    <button @click="showModal = !showModal" :data-title="desc" class="infocon-button">
      <Icon alt="Info" name="icons/info" adaptive />
    </button>
    <Modal :title="title" class="info-modal" :show="showModal" @dismiss="showModal = !showModal">
      <slot name="desc"></slot>
    </Modal>
  </div>
</template>

<script setup>
import { computed, useSlots, ref } from 'vue';

import Icon from './Icon.vue';
import Modal from './Modal.vue';

const slots = useSlots();
const showModal = ref(false);

const title = computed(() => {
  return slots.title()[0].children;
});
const desc = computed(() => {
  return slots.desc()[0].children;
});
</script>

<style scoped>
.infocon-container {
  display: block !important;
  transition: var(--theme-color-transition);
}

.infocon-button {
  padding: 0;
  background-color: transparent;
  border: none;
  line-height: 1rem;
  margin-left: 0.5rem;
}

@media (min-width: 768px) {
  .infocon-button:focus:before,
  .infocon-button:hover:before {
    content: ' ';
    position: absolute;
    top: -50%;
    left: 50%;
    z-index: 101;
    margin-left: -5px;
    border-width: 5px;
    border-style: solid;
    border-color: var(--c-background-mute) transparent transparent transparent;
  }

  .infocon-button:focus:after,
  .infocon-button:hover:after {
    content: attr(data-title);
    z-index: 100;
    background-color: var(--c-background-mute);
    color: var(--c-text);
    padding: 0.5rem 1rem;
    opacity: 0.9;
    border-radius: 0.5rem;
    border: 1px solid var(--c-box-border);
    box-shadow: 0 0 10px 0px var(--c-shadow);
    transform: translate(-50%, -100%);
    top: -50%;
    position: absolute;
    width: 50vw;
    max-width: max-content;
  }

  .infocon-container:deep(.info-modal) {
    display: none;
  }

  .infocon-container:deep(.backdrop) {
    display: none;
  }
}

@media (min-width: 1024px) {
}
</style>
