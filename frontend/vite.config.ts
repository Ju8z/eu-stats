import { fileURLToPath, URL } from 'node:url';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

const frontendRoot = fileURLToPath(new URL('.', import.meta.url));
const repoRoot = fileURLToPath(new URL('..', import.meta.url));

function normalizeBackendTarget(rawValue?: string): string | undefined {
    if (!rawValue) {
        return undefined;
    }

    try {
        const url = new URL(rawValue);

        if (url.hostname === '0.0.0.0') {
            url.hostname = 'localhost';
        }

        url.pathname = '';
        url.search = '';
        url.hash = '';

        return url.toString().replace(/\/$/, '');
    } catch {
        return rawValue.replace('0.0.0.0', 'localhost').replace(/\/api\/?$/, '');
    }
}

function resolveBackendTarget(env: Record<string, string | undefined>): string {
    return normalizeBackendTarget(env.VITE_BACKEND_TARGET)
        ?? normalizeBackendTarget(env.VITE_API_BASE_URL)
        ?? normalizeBackendTarget(env.APP_TRACKER_BASE_URL)
        ?? 'http://127.0.0.1:8080';
}

export default defineConfig(({ mode }) => {
    // Support running Vite from either the repo root or the frontend workspace.
    const env = {
        ...loadEnv(mode, repoRoot, ''),
        ...loadEnv(mode, frontendRoot, ''),
        ...process.env
    };
    const backendTarget = resolveBackendTarget(env);
    const proxy = {
        '/api': {
            target: backendTarget,
            changeOrigin: true
        },
        '/s.js': {
            target: backendTarget,
            changeOrigin: true
        }
    };

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
            proxy
        },
        preview: {
            host: '0.0.0.0',
            port: 5173,
            proxy
        }
    };
});
