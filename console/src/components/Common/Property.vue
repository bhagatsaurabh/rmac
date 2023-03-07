<template>
  <div :class="['property', state]">
    <div class="name"><slot></slot></div>
    <div class="value">
      <span class="input">
        <span v-if="state !== 'editing'">{{ state === 'syncing' ? value : orgValue }}</span>
        <input v-if="state === 'editing'" type="text" v-model="value" />
      </span>
      <span class="controls">
        <span class="edit" v-if="editable">
          <Button
            @click="handleEdit"
            v-if="state === 'idle' || state === 'syncing'"
            icon="edit"
            :complementary="false"
          />
          <Button
            @click="handleSave"
            v-if="state === 'editing' && orgValue !== value"
            icon="save"
          />
        </span>
        <Button @click="handleCopy" icon="copy" :complementary="false" />
      </span>
    </div>
    <span v-if="state === 'updating' || (state === 'syncing' && orgValue !== value)">
      <Spinner />
      <span>{{ state }}</span>
    </span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useStore } from 'vuex';
import Button from './Button.vue';
import Spinner from './Spinner.vue';

const store = useStore();

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
  name: {
    type: String,
    required: true,
  },
  editable: {
    type: Boolean,
    default: false,
  },
});

// States: Idle -> Editing -> Updating -> Error -> Idle
//                                     -> Notify: Update sent -> Waiting for Sync -> Idle
const orgValue = computed(() => store.getters.getHostById(props.id)[props.name]);

const state = ref('idle');
const value = ref(orgValue.value);

const handleEdit = () => {
  state.value = 'editing';
};
const handleSave = async () => {
  state.value = 'updating';
  await timeout(2000);
  state.value = 'syncing';
};
const handleCopy = () => {
  navigator.clipboard.writeText(orgValue.value);
};
</script>

<style scoped>
.property {
  /* border: 1px solid var(--c-border); */
  padding: 0.5rem;
  margin-bottom: 0.5rem;
}
.property .name {
  font-style: italic;
  font-size: 0.9rem;
}
.property .value {
  display: flex;
  align-items: center;
}
.property .value .input {
  flex: 1;
  font-size: 1.1rem;
  border: 1px solid var(--c-border-soft);
}
.controls {
  display: inline-block;
}
.controls button {
  padding: 0.5rem;
  border-radius: 0;
  display: inline-block;
  margin-left: 0.5rem;
}
</style>
