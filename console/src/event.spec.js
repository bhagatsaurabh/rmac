import { describe, it, expect, vi, afterEach } from 'vitest';
import bus from '@/event';

describe('Event Bus', () => {
  afterEach(() => {
    bus.clear();
  });

  it('should register events and callbacks', () => {
    const callback1 = vi.fn();
    const callback2 = vi.fn();
    const callback3 = vi.fn();

    bus.on('test-event-1', callback1);

    expect(bus.registeredEvents).toStrictEqual({
      'test-event-1': {
        0: callback1,
      },
    });

    bus.on('test-event-2', callback2);

    expect(bus.registeredEvents).toStrictEqual({
      'test-event-1': {
        0: callback1,
      },
      'test-event-2': {
        1: callback2,
      },
    });

    bus.on('test-event-1', callback3);

    expect(bus.registeredEvents).toStrictEqual({
      'test-event-1': {
        0: callback1,
        2: callback3,
      },
      'test-event-2': {
        1: callback2,
      },
    });
  });

  it('should execute callbacks', () => {
    const callback1 = vi.fn();
    const callback2 = vi.fn();

    bus.on('test-event-1', callback1);
    bus.on('test-event-2', callback2);

    bus.emit('test-event-2', { data: 'test-data', abc: 123 });

    expect(callback2).toHaveBeenCalledWith({ data: 'test-data', abc: 123 });

    bus.emit('test-event-1');

    expect(callback1).toHaveBeenCalled();
  });

  it('should de-register event callbacks', () => {
    const callback1 = vi.fn();
    const callback2 = vi.fn();

    const id = bus.on('test-event-1', callback1);
    bus.on('test-event-2', callback2);

    expect(bus.registeredEvents).toStrictEqual({
      'test-event-1': {
        0: callback1,
      },
      'test-event-2': {
        1: callback2,
      },
    });

    bus.off('test-event-1', id);

    expect(bus.registeredEvents).toStrictEqual({
      'test-event-1': {},
      'test-event-2': {
        1: callback2,
      },
    });
  });
});
