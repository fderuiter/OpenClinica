import 'prototype-js-core';
import 'scriptaculous-js';
import './setup-globals.js';

import 'headjs/dist/1.0.0/head.min.js';
import '@phagento/jquery-tmpl';
import 'jquery-migrate';
import 'jquery-blockui';

// Initialize the reactive store
import { store } from './store.js';

import 'json3';
import './vendor/jmesa/index.js';
import './vendor/new_cal/index.js';
import './vendor/wz_tooltip/wz_tooltip.js';
import './vendor/calendarpopup/CalendarPopup.js';

// Initialize modern React UI
import React from 'react';
import { createRoot } from 'react-dom/client';
import Navigation from './components/Navigation.jsx';
import CRFRenderer from './components/CRFRenderer.jsx';
import { AccessibilityProvider } from './components/AccessibilityProvider.jsx';

document.addEventListener('DOMContentLoaded', () => {
  // Mount the modern navigation menu if the container exists
  const menuContainer = document.getElementById('menuContainer');
  if (menuContainer) {
    const navRoot = createRoot(menuContainer);
    navRoot.render(
      React.createElement(AccessibilityProvider, null,
        React.createElement(Navigation, null)
      )
    );
  }

  // If this is the print CRF page, replace the body or specific element with the new renderer
  // (Detecting if we are in printable mode, similar to what app.js did)
  if (window.app_studyOID !== undefined && document.title.includes('Printable Forms') || window.location.pathname.includes('printcrf')) {
    const crfContainer = document.createElement('div');
    document.body.innerHTML = ''; // Replicate old app.js behavior of replacing body HTML
    document.body.appendChild(crfContainer);
    const crfRoot = createRoot(crfContainer);
    crfRoot.render(
      React.createElement(AccessibilityProvider, null,
        React.createElement(CRFRenderer, null)
      )
    );
  }
});
