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

// Initialize modern React UI (dynamically imported below)

async function mountReactApp() {
  const menuContainer = document.getElementById('menuContainer');
  const isPrintPage = (window.app_studyOID !== undefined && document.title.includes('Printable Forms')) || window.location.pathname.includes('printcrf');
  const crfContainer = document.getElementById('printCRFContainer');

  const needsNavigation = !!menuContainer;
  const needsCRF = !!(isPrintPage && crfContainer);

  if (!needsNavigation && !needsCRF) {
    return;
  }

  const [
    { default: React },
    { createRoot },
    { AccessibilityProvider }
  ] = await Promise.all([
    import('react'),
    import('react-dom/client'),
    import('./components/AccessibilityProvider.jsx')
  ]);

  // Mount the modern navigation menu if the container exists
  if (needsNavigation) {
    const Navigation = React.lazy(() => import('./components/Navigation.jsx'));
    const navRoot = createRoot(menuContainer);
    navRoot.render(
      React.createElement(
        AccessibilityProvider,
        null,
        React.createElement(
          React.Suspense,
          { fallback: React.createElement('div', null, 'Loading Navigation...') },
          React.createElement(Navigation, null)
        )
      )
    );
  }

  // If this is the print CRF page, replace the body or specific element with the new renderer
  if (needsCRF) {
    const CRFRenderer = React.lazy(() => import('./components/CRFRenderer.jsx'));
    const crfRoot = createRoot(crfContainer);
    crfRoot.render(
      React.createElement(
        AccessibilityProvider,
        null,
        React.createElement(
          React.Suspense,
          { fallback: React.createElement('div', null, 'Loading CRF...') },
          React.createElement(CRFRenderer, null)
        )
      )
    );
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', mountReactApp);
} else {
  mountReactApp();
}
