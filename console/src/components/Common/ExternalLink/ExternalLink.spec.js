import { describe, it, expect } from 'vitest';
import { shallowMount } from '@vue/test-utils';
import ExternalLink from '../ExternalLink/ExternalLink.vue';

describe('ExternalLink component', () => {
  let wrapper;

  it('should render', () => {
    wrapper = shallowMount(ExternalLink, {
      props: { to: 'test.com' },
      slots: {
        default: 'Test Link',
      },
    });

    expect(wrapper.vm).toBeDefined();
    expect(wrapper.find('button').text()).toStrictEqual('Test Link');
  });
});
