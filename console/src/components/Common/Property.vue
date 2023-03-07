<template>
  <div :class="['property', state]">
    <div class="name"><slot></slot></div>
    <div class="value">
      <span class="input">
        <span v-if="state !== 'editing'">{{ state === 'syncing' ? value : orgValue }}</span>
        <input
          @blur.passive="handleBlur"
          ref="inputEl"
          v-if="state === 'editing'"
          type="text"
          v-model="value"
          spellcheck="false"
        />
      </span>
      <span class="controls">
        <span class="edit" v-if="editable">
          <Button
            @click="handleEdit"
            v-if="
              state === 'idle' || state === 'syncing' || (state === 'editing' && orgValue === value)
            "
            icon="edit"
            :complementary="false"
          />
          <Button
            @click="handleSave"
            v-if="state === 'editing' && orgValue !== value"
            icon="save"
          />
        </span>
        <Button
          :class="{ copy: true, copied }"
          @click="handleCopy"
          icon="copy"
          :complementary="false"
        />
      </span>
    </div>
    <span v-if="state === 'updating' || (state === 'syncing' && orgValue !== value)">
      <Spinner />
      <span>{{ state }}</span>
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
//                                     -> Notify: Update sent -> Waiting for Sync -> Idle
const orgValue = computed(() => store.getters.getHostById(props.id)[props.name]);

const state = ref('idle');
const value = ref(orgValue.value);
const copied = ref(false);
const inputEl = ref(null);

const handleEdit = async () => {
  state.value = 'editing';
  await nextTick();
  inputEl.value?.focus();
};
const handleSave = async () => {
  state.value = 'updating';
  await timeout(2000);
  state.value = 'syncing';
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
const handleBlur = () => {
  state.value = 'idle';
  value.value = orgValue.value;
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
