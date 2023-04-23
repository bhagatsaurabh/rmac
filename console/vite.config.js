import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  build: {
    outDir: '../bridge-server/public',
    emptyOutDir: true,
  },
  test: {
    coverage: {
      reporter: ['json', 'html', 'lcov'],
    },
  },
  base: '/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
});
