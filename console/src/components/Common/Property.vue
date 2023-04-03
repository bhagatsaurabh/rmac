<template>
  <div :class="['property', state]">
    <div class="name"><slot></slot></div>
    <div class="value">
      <template v-if="inputType === 'text' || inputType === 'password' || inputType === 'number'">
        <span :class="{ input: true, disabled: !editable }" tabindex="0" @click="handleEdit">
          <span v-if="state !== 'editing'">
            {{ inputType === 'password' ? '******' : syncing ? value : orgValue }}
          </span>
          <input
            @blur="handleBlur"
            ref="inputEl"
            v-if="state === 'editing'"
            v-model="value"
            spellcheck="false"
            :type="inputType"
            :step="inputType === 'number' ? 1 : null"
          />
        </span>
      </template>
      <template v-else-if="inputType === 'checkbox'">
        <span :class="{ input: true, disabled: !editable }" tabindex="0">
          <Toggle
            @change="handleBlur"
            :ref="(instance) => instance?.inputElement"
            v-model="value"
            :id="name"
          >
            {{ value ? 'Enabled' : 'Disabled' }}
          </Toggle>
        </span>
      </template>
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
    <span class="update-spinner" v-if="state === 'updating' || syncing">
      <Spinner />
      <span>{{ syncing ? 'syncing' : state }}</span>
    </span>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, nextTick, watch, capitalize } from 'vue';
import { useStore } from 'vuex';
import Button from './Button.vue';
import Spinner from './Spinner.vue';
import Toggle from './Toggle.vue';

const store = useStore();

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
  type: {
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
  inputType: {
    type: String,
    default: 'text',
  },
});

// States: Idle -> Editing -> Updating -> Error -> Idle
//                                               -> Notify: Update sent -> Waiting for Sync -> Idle

const orgValue = computed(() => {
  if (props.type === 'global') return store.getters.getHostById(props.id)[props.name];
  else if (props.type === 'config') return store.getters.getHostById(props.id).config[props.name];
});

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
    prop: {
      name: capitalize(props.name),
      value: value.value,
    },
  });
  if (!result) {
    value.value = orgValue.value;
  } else {
    syncing.value = true;
  }
  state.value = 'idle';
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

watch(orgValue, () => {
  if (syncing.value && orgValue.value === value.value) {
    syncing.value = false;
  }
});

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
  justify-content: space-between;
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
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu,
    Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  color: var(--c-text);
  background-color: var(--c-background);
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
