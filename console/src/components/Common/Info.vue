<template>
  <div class="infocon-container">
    <button @click="showModal = !showModal" :data-title="title" class="infocon-button">
      <Icon class="icon" alt="Info" name="icons/info" :config="{ maxWidth: '1rem' }" adaptive />
    </button>
    <Modal class="info-modal" :show="showModal" @dismiss="showModal = !showModal">{{
      title
    }}</Modal>
  </div>
</template>

<script setup>
import { computed, useSlots, ref } from 'vue';

import Icon from './Icon.vue';
import Modal from './Modal.vue';

const slots = useSlots();
const showModal = ref(false);

const title = computed(() => {
  return slots.default()[0].children;
});
</script>

<style scoped>
.icon {
  pointer-events: none;
}
.infocon-button {
  padding: 0;
  background-color: transparent;
  border: none;
}

@media (min-width: 768px) {
  .infocon-button:focus:before,
  .infocon-button:hover:before {
    content: ' ';
    position: absolute;
    top: -50%;
    left: 50%;
    z-index: 99;
    margin-left: -5px;
    border-width: 5px;
    border-style: solid;
    border-color: #e9e9e9 transparent transparent transparent;
  }

  .infocon-button:focus:after,
  .infocon-button:hover:after {
    content: attr(data-title);
    z-index: 100;
    background-color: #e9e9e9;
    padding: 0.5rem 1rem;
    opacity: 0.9;
    border-radius: 0.5rem;
    border: 1px solid lightgrey;
    box-shadow: 0 0 10px 0px #ababab;
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
