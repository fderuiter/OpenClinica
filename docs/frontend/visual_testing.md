# Visual Testing Guide

## Automated Build Behavior

The visual test browser binaries are now automatically downloaded within the standard Maven build lifecycle. When developers run a standard `mvn clean install` or when the `OpenClinica-visual-tests` module is built, the necessary target browsers (Chromium, Firefox, WebKit) are installed automatically via Playwright.

This eliminates the need for manual command-line setups or global dependencies to run local integration visual tests. The visual testing environment is automatically prepared without manual browser installation.

### Configuration Options

If you need to work completely offline, or wish to skip browser downloads during local compilation, you can toggle the behavior via the Maven property `playwright.skip.browser.download`.

To skip the automated browser downloads, append the flag:
`mvn clean install -Dplaywright.skip.browser.download=true`

By default, the flag is evaluated to `false`, ensuring zero manual setup for clean workspace environments.

## Running Tests Locally

You can use the dedicated package scripts located in the testing package configuration (`visual-tests/package.json`) to execute visual tests natively.

- **Run all visual regression tests:**
  `npm run test:visual` (or `npm test`)

- **Update visual regression baselines:**
  `npm run test:visual:update`

These commands automatically use the local Playwright configuration and execute tests against your frontend updates.
