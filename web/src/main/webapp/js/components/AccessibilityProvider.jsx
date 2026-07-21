import React, { createContext, useState, useContext, useCallback, useRef } from 'react';

const AccessibilityContext = createContext({
  announce: (message) => {},
});

export const useAccessibility = () => useContext(AccessibilityContext);

export function AccessibilityProvider({ children }) {
  const [announcement, setAnnouncement] = useState('');
  const queueRef = useRef([]);
  const isProcessingRef = useRef(false);
  const toggleRef = useRef(false);

  const processQueue = useCallback(() => {
    if (queueRef.current.length === 0) {
      isProcessingRef.current = false;
      return;
    }

    isProcessingRef.current = true;
    const message = queueRef.current.shift();
    
    toggleRef.current = !toggleRef.current;
    const suffix = toggleRef.current ? '\u200B' : '';
    setAnnouncement(message + suffix);

    setTimeout(() => {
      processQueue();
    }, 200);
  }, []);

  const announce = useCallback((message) => {
    queueRef.current.push(message);
    if (!isProcessingRef.current) {
      processQueue();
    }
  }, [processQueue]);

  return (
    <AccessibilityContext.Provider value={{ announce }}>
      {children}
      <div
        aria-live="polite"
        aria-atomic="true"
        style={{
          position: 'absolute',
          width: '1px',
          height: '1px',
          padding: '0',
          margin: '-1px',
          overflow: 'hidden',
          clip: 'rect(0, 0, 0, 0)',
          whiteSpace: 'nowrap',
          border: '0',
        }}
      >
        {announcement}
      </div>
    </AccessibilityContext.Provider>
  );
}
