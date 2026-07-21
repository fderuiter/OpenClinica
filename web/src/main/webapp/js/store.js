import { useSyncExternalStore } from 'react';

export const store = {
  state: {
    studyOID: window.app_studyOID || '',
    userSession: window.app_userSession || '',
    formData: {},
    errors: {},
  },
  listeners: [],
  setState(newState) {
    this.state = { ...this.state, ...newState };
    this.listeners.forEach((listener) => listener(this.state));
  },
  setFormData(groupOID, index, fieldOID, value) {
    const currentData = { ...this.state.formData };
    if (!currentData[groupOID]) {
      currentData[groupOID] = [];
    }

    // Ensure the array is a copy
    currentData[groupOID] = [...currentData[groupOID]];

    if (!currentData[groupOID][index]) {
      currentData[groupOID][index] = {};
    } else {
      currentData[groupOID][index] = { ...currentData[groupOID][index] };
    }

    currentData[groupOID][index][fieldOID] = value;
    this.setState({ formData: currentData });
  },
  addRow(groupOID, schema) {
    const currentData = { ...this.state.formData };
    if (!currentData[groupOID]) {
      currentData[groupOID] = [];
    } else {
      currentData[groupOID] = [...currentData[groupOID]];
    }

    // Initialize with empty values and proper formatting automatically
    const newRow = {};
    const groupSchema = schema.groups.find((g) => g.groupOID === groupOID);
    if (groupSchema) {
      groupSchema.fields.forEach((field) => {
        newRow[field.fieldOID] = '';
      });
    }

    currentData[groupOID].push(newRow);
    this.setState({ formData: currentData });
  },
  removeRow(groupOID, index) {
    const currentData = { ...this.state.formData };
    if (currentData[groupOID]) {
      currentData[groupOID] = currentData[groupOID].filter(
        (_, i) => i !== index
      );
      this.setState({ formData: currentData });
    }
  },
  subscribe(listener) {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter((l) => l !== listener);
    };
  },
  getState() {
    return this.state;
  },
};

// Legacy non-blocking bridge
let initialStudyOID = window.app_studyOID;
if (initialStudyOID !== undefined) {
  store.setState({ studyOID: initialStudyOID });
}

Object.defineProperty(window, 'app_studyOID', {
  get() {
    return store.getState().studyOID;
  },
  set(val) {
    if (store.getState().studyOID !== val) {
      store.setState({ studyOID: val });
    }
  },
  configurable: true,
});

export function useStore(selector) {
  return useSyncExternalStore(
    (listener) => store.subscribe(listener),
    () => selector(store.getState())
  );
}
