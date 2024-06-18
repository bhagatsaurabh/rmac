<template>
  <div class="commands">
    <span class="mb-0p5 mr-0p5">RMAC Command</span>
    <Button @click="() => handleCommand('system shutdown')">shutdown</Button>
    <br />
    <form @submit.prevent="() => handleCommand(inputCommand)">
      <input :disabled="sending" v-model="inputCommand" type="text" spellcheck="false" />
    </form>
    <Button @click="() => handleCommand(inputCommand)" class="ml-0p5" icon="right-arrow" icon-left>
      Send
    </Button>
    <Spinner v-if="sending" class="ml-0p5" />
    <label></label>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useStore } from 'vuex';
import Button from '../Common/Button/Button.vue';
import Spinner from '../Common/Spinner/Spinner.vue';

const store = useStore();

const props = defineProps({
  host: {
    type: Object,
    required: true,
  },
});

const sending = ref(false);
const inputCommand = ref('');

const handleCommand = async (command) => {
  inputCommand.value = '';
  sending.value = true;
  await store.dispatch('sendCommand', { hostId: props.host.id, command });
  sending.value = false;
};
</script>

<style scoped>
.commands {
  height: 5rem;
  padding: 0.5rem;
  z-index: 2;
  border-bottom: 1px solid var(--c-border);
}
.commands:deep(button) {
  display: inline-block;
  padding: 0.25rem 0.5rem;
}
.commands:deep(button:not(:last-child)) {
  margin-right: 0.5rem;
}
.commands form {
  display: inline-block;
}
.commands input {
  padding: 0.2rem 0.5rem;
  margin-top: 0.5rem;
  color: var(--c-text);
  background-color: var(--c-background);
  border: 1px solid var(--c-text);
  border-radius: 1rem;
}
</style>
