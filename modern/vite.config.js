import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  build: {
    outDir: 'target/classes/static',
    emptyOutDir: false,
    rollupOptions: {
      input: {
        listuseraccounts: resolve(__dirname, 'listuseraccounts.html')
      }
    }
  }
});
