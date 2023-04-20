import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import Toggle from '../Toggle/Toggle.vue';

describe('Toggle component', () => {
  let wrapper;
  beforeEach(() => {
    wrapper = mount(Toggle, {
      props: {
        id: 'test-toggle',
      },
    });
  });

  it('should render', () => {
    expect(wrapper.vm).toBeDefined();
  });

  it('should emit change when value changes as a result of toggle', async () => {
    expect(wrapper.vm.value).toStrictEqual(false);

    await wrapper.find('button.switch').trigger('click');
    await wrapper.find('input').setValue(true);

    expect(wrapper.emitted()['update:modelValue']).toBeTruthy();
  });
});
