class EventBus {
  constructor() {
    this.registeredEvents = {};
    this.lastId = 0;
  }

  on(eventKey, callback) {
    if (!this.registeredEvents[eventKey]) this.registeredEvents[eventKey] = {};

    let id = this.lastId;
    this.registeredEvents[eventKey][id] = callback;
    this.lastId += 1;
    return id;
  }
  emit(eventKey, ...args) {
    if (this.registeredEvents[eventKey]) {
      if (!args) args = [];
      Object.values(this.registeredEvents[eventKey]).forEach((callback) => callback(...args));
    }
  }

  off(eventKey, id) {
    if (this.registeredEvents[eventKey]) delete this.registeredEvents[eventKey][id];
  }

  clear() {
    this.registeredEvents = {};
  }
}

export default new EventBus();
