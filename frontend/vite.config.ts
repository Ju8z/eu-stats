import { fileURLToPath, URL } from 'node:url';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), '');
    const backendTarget = env.VITE_BACKEND_TARGET ?? 'http://0.0.0.0:8080';

    return {
        plugins: [react()],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url))
            }
        },
        server: {
            host: '0.0.0.0',
            port: 5173,
            proxy: {
                '/api': {
                    target: backendTarget,
                    changeOrigin: true
                },
                '/s.js': {
                    target: backendTarget,
                    changeOrigin: true
                }
            }
        },
        preview: {
            host: '0.0.0.0',
            port: 5173,
            proxy: {
                '/api': {
                    target: backendTarget,
                    changeOrigin: true
                },
                '/s.js': {
                    target: backendTarget,
                    changeOrigin: true
                }
            }
        }
    };
});
