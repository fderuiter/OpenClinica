# Frontend Architecture

This document outlines the modern frontend architecture, detailing the asset injection process, entry points, legacy library bridges, and state synchronization.

## 1. Asset Injection Process

The application bridges legacy JSP pages and modern React development using an asset injection build step.

- **Vite Build:** First, `vite build` processes modern Javascript and CSS files into optimized static assets. This uses Vite with the `@vitejs/plugin-react` plugin.
- **`inject-bundle.js` script:** This script is executed post-build. It reads Vite's `manifest.json` to identify the resulting `main.js` chunk.
- **Rewriting Script Tags:** `inject-bundle.js` scans `.jsp`, `.html`, and `load_scripts.js` files. It replaces legacy script tag inclusions (such as `prototype.js`, `scriptaculous`, `effects.js`, `ua-parser.min.js`) with the newly built React/Vite chunk (`main.js`). 
- **Output:** The updated files are saved to `target/vite-resources`, replacing manual inclusion of dozens of scripts and ensuring the modern bundle runs immediately.

## 2. Modern Entry Points & Legacy Bridges

The single modern entry point is `main.js`, replacing numerous legacy initialization scripts.

- **Legacy Library Bridge:** `main.js` imports global dependencies such as jQuery, Underscore, dateFormat, HeadJS, UAParser, jmesa, and calendar popup libraries at the top of the file. It assigns these modules to the `window` object so that existing global code in JSPs can still access `window.jQuery`, `window._`, `window.dateFormat`, etc., seamlessly.
- **React Entry:** After initializing legacy plugins, `main.js` sets up React 19 components using `react-dom/client`.
- **Mounting:** 
  - `Navigation.jsx` is mounted if the `menuContainer` element exists.
  - `CRFRenderer.jsx` is mounted on the body if the URL contains `printcrf` or the document title indicates a printable form.

## 3. State Synchronization Bridge (JSP to React)

To provide reactive data flow from legacy Java Server Pages (JSP) into React components, the architecture relies on `store.js`.

- **Reactive Store:** `store.js` implements a simple reactive store using `subscribe` and `setState` patterns. React components can subscribe to it.
- **Property Interception:** JSP pages traditionally assign data directly to global variables like `window.app_studyOID`. The modern bridge intercepts these assignments using `Object.defineProperty` on the `window` object.
- **Synchronization Flow:**
  - When legacy code runs `window.app_studyOID = '123';`, the custom setter captures the update and calls `store.setState({ studyOID: '123' })`.
  - Listeners (like React hooks or component methods) subscribed to `store.listeners` are notified immediately and rerender with the new data.
  - Reading `window.app_studyOID` proxies to `store.getState().studyOID`.
## 4. Local Development Scripts

To replace manual execution, standard npm scripts are available in the `web` directory:

- `npm run dev`: Starts the Vite development server for local hot-module reloading of React components.
- `npm run build`: Executes the optimized production build and triggers the `inject-bundle.js` script to update legacy asset inclusion.
- `npm run docs`: Generates the automated API reference documentation for the frontend components.
