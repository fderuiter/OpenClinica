import React, { useEffect, useState } from 'react';
import { store } from '../store';
import styles from './Navigation.module.css';

export default function Navigation() {
  const [studyOID, setStudyOID] = useState(store.getState().studyOID);

  useEffect(() => {
    return store.subscribe((state) => {
      setStudyOID(state.studyOID);
    });
  }, []);

  return (
    <nav className={`modern-navigation ${styles.nav}`}>
      <ul className={styles.list}>
        <li>
          <a href="/" className={styles.link}>
            Home
          </a>
        </li>
        <li>
          <a href="/manage" className={styles.link}>
            Manage Study
          </a>
        </li>
        <li className={styles.currentStudy}>
          <strong>Current Study OID:</strong> {studyOID || 'None Selected'}
        </li>
      </ul>
    </nav>
  );
}
