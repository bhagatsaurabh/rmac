<template>
  <div :class="['property', state]">
    <div class="name"><slot></slot></div>
    <div class="value">
      <span :class="{ input: true, disabled: !editable }" tabindex="0" @click="handleEdit">
        <span v-if="state !== 'editing'">{{ syncing ? value : orgValue }}</span>
        <input
          @blur="handleBlur"
          ref="inputEl"
          v-if="state === 'editing'"
          type="text"
          v-model="value"
          spellcheck="false"
        />
      </span>
      <span class="controls">
        <Button
          @click="handleSave"
          v-if="editable && (state === 'editing' || state === 'edited') && orgValue !== value"
          icon="save"
        />
        <Button
          :class="{ copy: true, copied }"
          @click="handleCopy"
          icon="copy"
          :complementary="false"
        />
      </span>
    </div>
    <span class="update-spinner" v-if="state === 'updating' || (syncing && orgValue !== value)">
      <Spinner />
      <span>{{ syncing ? 'syncing' : state }}</span>
    </span>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, nextTick } from 'vue';
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
//                                               -> Notify: Update sent -> Waiting for Sync -> Idle

const orgValue = computed(() => store.getters.getHostById(props.id)[props.name]);

const state = ref('idle');
const syncing = ref(false);
const value = ref(orgValue.value);
const copied = ref(false);
const inputEl = ref(null);

const handleEdit = async () => {
  if (!props.editable) return;

  state.value = 'editing';
  await nextTick();
  inputEl.value?.focus();
};
const handleSave = async () => {
  state.value = 'updating';
  syncing.value = false;
  const result = await store.dispatch('updateProperty', {
    id: props.id,
    prop: { name: props.name, value: value.value },
  });
  if (!result) {
    state.value = 'idle';
    value.value = orgValue.value;
  } else {
    syncing.value = true;
  }
};
const handleBlur = () => {
  if (value.value === orgValue.value) {
    state.value = 'idle';
  } else {
    state.value = 'edited';
  }
};
let timerHandle;
const handleCopy = () => {
  navigator.clipboard.writeText(orgValue.value);

  clearTimeout(timerHandle);
  copied.value = true;
  timerHandle = setTimeout(() => {
    copied.value = false;
  }, 3000);
};

onBeforeUnmount(() => {
  clearTimeout(timerHandle);
});
</script>

<style scoped>
.property {
  padding: 0.5rem;
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
  padding: 0.1rem 0.5rem;
  width: 100%;
  min-width: 0;
  transition: box-shadow var(--fx-transition-duration) linear;
}
.property.editing .input {
  box-shadow: 0 0 10px 0 var(--c-shadow);
}
.property .input.disabled {
  background-color: var(--c-background-mute);
}
.property .value .input span {
  width: 100%;
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
}

.property .value .input input {
  border: 0;
  width: 100%;
  font-size: 1.1rem;
  padding-left: 0;
}
.property .value .input input:focus {
  outline: none;
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
.controls .copy::before {
  opacity: 0;
  content: 'Copied';
  position: absolute;
  top: -100%;
  left: 50%;
  border-radius: 1rem;
  transform: translateX(-50%);
  padding: 0.2rem 0.5rem;
  background-color: var(--c-background-mute);
  border: 1px solid var(--c-border);
  animation-duration: 3s;
  animation-timing-function: linear;
}
.controls .copy:focus-visible::before {
  outline: none;
}
.controls .copy.copied::before {
  animation-name: flash;
}

.update-spinner span:last-of-type {
  margin-left: 0.3rem;
}

.update-spinner:deep(.icon-container) {
  vertical-align: middle;
}

@keyframes flash {
  0% {
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}
</style>
