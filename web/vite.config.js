import { defineConfig } from 'vite';
import { resolve } from 'path';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [
    react(),
    {
      name: 'patch-prototype',
      enforce: 'pre',
      transform(code, id) {
        if (id.includes('calendar.js') && !id.includes('calendar-en.js')) {
          return 'var Calendar;\n' + code + '\nwindow.Calendar = Calendar;';
        }
        if (id.includes('prototype.js')) {
          let patched = code.replace(/\}\)\(this\);/g, '})(window);');
          patched = patched.replace(
            /Event\.prototype = window\.Event\.prototype \|\| document\.createEvent\('HTMLEvents'\)\.__proto__;/g,
            'try { Event.prototype = window.Event.prototype || document.createEvent("HTMLEvents").__proto__; } catch (e) {}'
          );
          patched = patched.replace(
            /if \(GLOBAL\.Event\) Object\.extend\(window\.Event, Event\);/g,
            'if (GLOBAL.Event) { var _E = Object.assign({}, Event); delete _E.prototype; Object.extend(window.Event, _E); }'
          );
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
            if (
              id.includes('prototype-js-core') ||
              id.includes('scriptaculous-js') ||
              id.includes('jquery')
            ) {
              return 'legacy-vendor';
            }
            if (id.includes('react') || id.includes('react-dom')) {
              return 'react-vendor';
            }
            return 'vendor';
          }
        },
      },
    },
  },
});
