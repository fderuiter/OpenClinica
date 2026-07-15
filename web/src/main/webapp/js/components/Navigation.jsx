import React, { useEffect, useState } from 'react';
import { store } from '../store';
import { THEME } from '../theme';

export default function Navigation() {
  const [studyOID, setStudyOID] = useState(store.getState().studyOID);

  useEffect(() => {
    return store.subscribe((state) => {
      setStudyOID(state.studyOID);
    });
  }, []);

  return (
    <nav className="modern-navigation" style={{ padding: '10px', background: '#f4f4f4', borderBottom: `1px solid ${THEME.colors.border}` }}>
      <ul style={{ listStyleType: 'none', margin: 0, padding: 0, display: 'flex', gap: '20px', alignItems: 'center' }}>
        <li><a href="#" style={{ textDecoration: 'none', color: '#333', fontWeight: 'bold' }}>Home</a></li>
        <li><a href="#" style={{ textDecoration: 'none', color: '#333', fontWeight: 'bold' }}>Manage Study</a></li>
        <li style={{ marginLeft: 'auto' }}>
          <strong>Current Study OID:</strong> {studyOID || 'None Selected'}
        </li>
      </ul>
    </nav>
  );
}
