import { defineConfig } from 'vite';
import { resolve } from 'path';
import react from '@vitejs/plugin-react';
import checker from 'vite-plugin-checker';
import { execSync } from 'child_process';
import fs from 'fs';

function automatedSchemaCompiler() {
  return {
    name: 'automated-schema-compiler',
    buildStart() {
      // 1. Compile OpenAPI to d.ts automatically
      const openapiPath = resolve(__dirname, '../docs/openapi.json');
      const outDir = resolve(__dirname, 'src/types');
      if (!fs.existsSync(outDir)) {
        fs.mkdirSync(outDir, { recursive: true });
      }
      const outPath = resolve(outDir, 'api.d.ts');
      console.log('Compiling OpenAPI to TypeScript definitions...');
      execSync(`npx openapi-typescript ${openapiPath} -o ${outPath}`);

      // 2. Extract store parameters, window properties, inline form layouts
      const storeCode = fs.readFileSync(resolve(__dirname, 'src/main/webapp/js/store.js'), 'utf-8');
      const crfCode = fs.readFileSync(resolve(__dirname, 'src/main/webapp/js/components/CRFRenderer.jsx'), 'utf-8');

      // simple regex extraction
      const storeKeysMatch = storeCode.match(/state:\s*{([^}]+)}/);
      const storeKeys = storeKeysMatch ? storeKeysMatch[1]
        .split('\\n')
        .map(line => line.trim().split(':')[0])
        .filter(k => k && !k.startsWith('//')) : [];

      const windowProps = [...crfCode.matchAll(/window\.app_[a-zA-Z0-9_]+/g)]
        .map(m => m[0])
        .concat([...storeCode.matchAll(/window\.app_[a-zA-Z0-9_]+/g)].map(m => m[0]))
        .filter((v, i, a) => a.indexOf(v) === i); // unique

      // extract inline schema from CRF
      const schemaMatch = crfCode.match(/const schema = ({[\\s\\S]*?});\\n\\nconst getFieldDiscrepancy/);
      let formLayouts = {};
      if (schemaMatch) {
        formLayouts = (new Function(`return ${schemaMatch[1]}`))();
      }

      const unifiedSchema = {
        storeParameters: ['studyOID', 'userSession', 'formData', 'errors'],
        globalWindowProperties: windowProps,
        inlineFormLayouts: formLayouts
      };

      const distDir = resolve(__dirname, 'src/main/webapp/dist');
      if (!fs.existsSync(distDir)) {
        fs.mkdirSync(distDir, { recursive: true });
      }
      fs.writeFileSync(
        resolve(distDir, 'unified-schema.json'),
        JSON.stringify(unifiedSchema, null, 2)
      );
      
      // Provide definitions for TS checker
      const globalDts = `
declare module '*.module.css';
interface Window {
  ${windowProps.map(prop => `${prop.replace('window.', '')}: any;`).join('\n  ')}
  debug: any;
  util_logDebug: any;
}
declare class ErrorBoundary extends React.Component<any, any> {}
`;
      fs.writeFileSync(resolve(outDir, 'global.d.ts'), globalDts);
    },
    closeBundle() {
      // Regenerate or write the schema here so it survives emptyOutDir
      const storeCode = fs.readFileSync(resolve(__dirname, 'src/main/webapp/js/store.js'), 'utf-8');
      const crfCode = fs.readFileSync(resolve(__dirname, 'src/main/webapp/js/components/CRFRenderer.jsx'), 'utf-8');

      const windowProps = [...crfCode.matchAll(/window\.app_[a-zA-Z0-9_]+/g)]
        .map(m => m[0])
        .concat([...storeCode.matchAll(/window\.app_[a-zA-Z0-9_]+/g)].map(m => m[0]))
        .filter((v, i, a) => a.indexOf(v) === i);

      const schemaMatch = crfCode.match(/const schema = ({[\s\S]*?});\n\nconst getFieldDiscrepancy/);
      let formLayouts = {};
      if (schemaMatch) {
        formLayouts = (new Function(`return ${schemaMatch[1]}`))();
      }

      const unifiedSchema = {
        storeParameters: ['studyOID', 'userSession', 'formData', 'errors'],
        globalWindowProperties: windowProps,
        inlineFormLayouts: formLayouts
      };

      const distDir = resolve(__dirname, 'src/main/webapp/dist');
      if (!fs.existsSync(distDir)) {
        fs.mkdirSync(distDir, { recursive: true });
      }
      fs.writeFileSync(
        resolve(distDir, 'unified-schema.json'),
        JSON.stringify(unifiedSchema, null, 2)
      );
    }
  };
}

export default defineConfig({
  plugins: [
    react(),
    automatedSchemaCompiler(),
    checker({
      typescript: true,
      overlay: false
    }),
    {
      name: 'patch-prototype',
      transform(code, id) {
        if (id.includes('prototype.js')) {
          let patched = code.replace(/\}\)\(this\);/g, '})(window);');
          patched += `\nwindow.Prototype = Prototype;
window.Class = Class;
if (typeof $ !== 'undefined') window.$ = $;
if (typeof $$ !== 'undefined') window.$$ = $$;
window.Element = Element;
window.Event = Event;
window.Ajax = Ajax;
window.Form = Form;
window.Field = Field;
window.Position = Position;
window.Toggle = Toggle;
window.Insertion = Insertion;
window.Abstract = Abstract;
window.Try = Try;
window.Hash = Hash;
window.Enumerable = Enumerable;`;
          return patched;
        }
      },
    },
  ],
  build: {
    outDir: 'src/main/webapp/dist',
    emptyOutDir: true,
    manifest: true,
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'src/main/webapp/js/main.js'),
      },
      output: {
        format: 'es',
        entryFileNames: 'assets/[name]-[hash].js',
        chunkFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]',
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('prototype-js-core') || id.includes('scriptaculous-js') || id.includes('jquery')) {
              return 'legacy-vendor';
            }
            if (id.includes('react') || id.includes('react-dom')) {
              return 'react-vendor';
            }
            return 'vendor';
          }
        }
      },
    },
  },
});
