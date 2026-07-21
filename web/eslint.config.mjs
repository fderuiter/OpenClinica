import js from "@eslint/js";
import reactPlugin from "eslint-plugin-react";
import jsxA11yPlugin from "eslint-plugin-jsx-a11y";
import globals from "globals";

export default [
  js.configs.recommended,
  {
    ignores: ["node_modules/**", "**/target/**", "**/dist/**", "src/main/webapp/js/vendor/**", "src/main/webapp/includes/**"]
  },
  {
    files: ["**/*.js", "**/*.jsx"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
      parserOptions: {
        ecmaFeatures: {
          jsx: true
        }
      },
      globals: {
        ...globals.browser,
        ...globals.node,
        ...globals.jest,
        "app_contextPath": "readonly",
        "app_logLevel": "readonly",
        "idleTime": "readonly",
        "DO_AUTO_LOGOUT": "readonly",
        "DO_AUTO_SERVER_LOGOUT": "readonly",
        "user": "readonly",
        "util_logout": "readonly",
        "$": "readonly",
        "head": "readonly"
      }
    },
    plugins: {
      react: reactPlugin,
      "jsx-a11y": jsxA11yPlugin
    },
    rules: {
      ...reactPlugin.configs.recommended.rules,
      ...jsxA11yPlugin.configs.recommended.rules,
      "react/react-in-jsx-scope": "off",
      "react/prop-types": "off"
    },
    settings: {
      react: {
        version: "detect"
      }
    }
  }
];
