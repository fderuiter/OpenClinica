export const store = {
  state: {
    studyOID: window.app_studyOID || '',
    userSession: window.app_userSession || '',
  },
  listeners: [],
  setState(newState) {
    this.state = { ...this.state, ...newState };
    this.listeners.forEach(listener => listener(this.state));
  },
  subscribe(listener) {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  },
  getState() {
    return this.state;
  }
};

// Legacy non-blocking bridge
// This intercepts assignments to window.app_studyOID by legacy JSP pages
// and synchronizes them with the reactive store, and vice versa.
let initialStudyOID = window.app_studyOID;
if (initialStudyOID !== undefined) {
  store.setState({ studyOID: initialStudyOID });
}

Object.defineProperty(window, 'app_studyOID', {
  get() { return store.getState().studyOID; },
  set(val) {
    if (store.getState().studyOID !== val) {
      store.setState({ studyOID: val });
    }
  },
  configurable: true
});
