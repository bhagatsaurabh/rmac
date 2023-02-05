<template>
  <Backdrop :show="show" @dismiss="$emit('dismiss')" />
  <Transition v-bind="$attrs" name="modal">
    <div v-if="show" class="modal">
      <Icon
        v-if="!(yes || no || ok || close)"
        class="close-icon"
        alt="Close icon"
        name="icons/cancel"
        :config="{ maxWidth: '1rem' }"
        adaptive
        @click="$emit('dismiss')"
      />
      <div class="content"><slot></slot></div>
      <div v-if="yes || no || ok || close" class="controls">
        <button v-if="yes" class="control">Yes</button>
        <button v-if="no" class="control">No</button>
        <button v-if="ok" class="control">OK</button>
        <button v-if="close" class="control">Close</button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import Backdrop from './Backdrop.vue';
import Icon from './Icon.vue';

defineEmits(['dismiss']);

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  yes: {
    type: Boolean,
    default: false,
  },
  no: {
    type: Boolean,
    default: false,
  },
  ok: {
    type: Boolean,
    default: false,
  },
  close: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
.modal {
  position: fixed;
  z-index: 102;
  border: 1px solid;
  box-shadow: 0 0 10px 0px #b3b3b3;
  left: 10vw;
  right: 10vw;
  top: 50vh;
  opacity: 1;
  background-color: #f1f1f1;
  transform: translateY(-50%);
  padding: 0.3rem 0.3rem 1rem 0.3rem;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal .content {
  padding: 0 0.2rem;
}
.close-icon {
  position: relative;
  right: 0;
  font-size: 0;
  width: fit-content;
  margin-left: auto;
  margin-bottom: 0.5rem;
}
</style>
