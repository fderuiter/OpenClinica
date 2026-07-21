import React from 'react';
import { useStore } from '../store';
import styles from './Navigation.module.css';

export default function Navigation() {
  const studyOID = useStore(state => state.studyOID);

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
