import { describe, expect, it, vi } from 'vitest';
import { debounce, defaultHeaders, rand, timeout, timeoutFn } from './utils';

describe('The utils', () => {
  it('should debounce', () => {
    vi.useFakeTimers();

    const debouncee = vi.fn();

    debounce(debouncee, 250)();
    vi.advanceTimersByTime(100);
    debounce(debouncee, 250)();
    vi.advanceTimersByTime(100);
    debounce(debouncee, 250)();
    vi.advanceTimersByTime(100);
    debounce(debouncee, 250)();
    vi.advanceTimersByTime(100);
    debounce(debouncee, 250)();

    expect(debouncee).toHaveBeenCalledTimes(2);

    debounce(debouncee, 250)();
    vi.advanceTimersByTime(250);
    debounce(debouncee, 250)();
    vi.advanceTimersByTime(250);
    debounce(debouncee, 250)();
    vi.advanceTimersByTime(250);
    debounce(debouncee, 250)();
    vi.advanceTimersByTime(250);
    debounce(debouncee, 250)();

    expect(debouncee).toHaveBeenCalledTimes(9);

    vi.useRealTimers();
  });

  it('should delay', async () => {
    vi.useFakeTimers();

    const start = new Date().getTime();
    vi.advanceTimersByTimeAsync(1200);
    await timeout(1200);
    const end = new Date().getTime();

    expect(end - start).toBeGreaterThanOrEqual(1200);

    vi.useRealTimers();
  });

  it('should delay function execution', async () => {
    vi.useFakeTimers();
    const callback = vi.fn();

    const start = new Date().getTime();
    vi.advanceTimersByTimeAsync(500);
    await timeoutFn(callback, 500);
    const end = new Date().getTime();

    expect(end - start).toBeGreaterThanOrEqual(500);
    expect(callback).toHaveBeenCalled();

    vi.useRealTimers();
  });

  it('should generate random numbers', () => {
    const numbers = Array(100)
      .fill(0)
      .map((_, _i) => rand(0, 1000000));

    expect(new Set(numbers).size).toStrictEqual(100);
  });

  it('should create correct default headers', () => {
    const headers = defaultHeaders();

    expect(headers).toStrictEqual({
      Accept: 'application/json',
      'Content-Type': 'application/json',
    });
  });
});
