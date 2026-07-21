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

---

# Backend Architecture

This section details the backend system structure, defining the responsibilities, boundaries, and dependencies of the five primary Maven modules.

## 1. System Modules & Responsibilities

The backend is composed of five core modules:

1. **`domain`**: Contains the core business logic, entities, and domain model. It represents the innermost layer of the application.
2. **`core`**: Contains core infrastructure, data access objects (DAOs), persistence configurations, and database integrations. 
3. **`web`**: The traditional web layer, containing legacy JSP integrations and Spring MVC controllers for the web interface.
4. **`ws`**: The Web Services module, responsible for SOAP and legacy API integrations.
5. **`modern`**: The modern API layer containing REST controllers and the Vite/React frontend entry points.

## 2. Dependency Flow and Module Boundaries

To prevent dependency rot and codebase degradation, the system enforces a strict dependency hierarchy:

- **`domain` Module Rules:** The domain module must remain completely isolated from infrastructure and external frameworks. It **must not** depend on any classes from the `core`, `web`, `ws`, or `modern` modules. Furthermore, it is restricted from using persistence frameworks (e.g., Hibernate, JPA) or web frameworks (e.g., Spring Web, Servlets).
- **`core` Module Rules:** The core module handles persistence and infrastructure, and naturally depends on the `domain` module.
- **`web`, `ws`, and `modern` Module Rules:** These outward-facing modules orchestrate web requests and API endpoints, and they depend on both `core` and `domain` to process business logic.

## 3. Automated Architectural Validation

To enforce these boundaries, we use automated architecture tests within our standard test suite.

- **ArchUnit Tests:** The `domain` module includes a JUnit-based test suite (using `ArchUnit`) that verifies structural rules during the build process.
- **Rule Enforcement:** If a developer accidentally imports a forbidden package (such as `org.springframework..` or `org.akaza.openclinica.dao..`) into the domain module, the `DomainArchitectureTest` will automatically fail the build, pointing precisely to the illegal dependency.
- **Continuous Integration:** These architectural assertions run locally via Maven (`mvn test -pl domain`) and as a standard check in our CI pipelines, ensuring the domain layer remains decoupled.
