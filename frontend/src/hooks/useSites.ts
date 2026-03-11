import { useCallback, useEffect, useRef, useState } from 'react';
import { sitesApi } from '@/api/sitesApi';
import type { Site } from '@/types/site';

const REFRESH_INTERVAL_MILLIS = 5000;
const LOAD_ERROR_MESSAGE = 'Failed to load sites. Please check if backend and database are running.';

/**
 * Centralizes site-list loading and retry behavior.
 * The hook keeps loading state, failure messages, and refresh policy out of page components so the
 * dashboard view stays mostly declarative.
 */
export function useSites() {
    const [sites, setSites] = useState<Site[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const hasLoadedRef = useRef(false);

    const loadSites = useCallback(async(showLoading: boolean) => {
        if (showLoading) {
            setIsLoading(true);
        }

        try {
            const result = await sitesApi.list();
            setSites(result.sites);
            setError(null);
            hasLoadedRef.current = true;
        } catch (error_) {
            console.error('Failed to load sites:', error_);
            if (!hasLoadedRef.current) {
                setSites([]);
                setError(LOAD_ERROR_MESSAGE);
            }
        } finally {
            if (showLoading) {
                setIsLoading(false);
            }
        }
    }, []);

    useEffect(() => {
        void loadSites(true);

        const refreshWhenVisible = () => {
            if (globalThis.document.visibilityState === 'visible') {
                void loadSites(false);
            }
        };

        const intervalId = globalThis.window.setInterval(() => {
            refreshWhenVisible();
        }, REFRESH_INTERVAL_MILLIS);

        globalThis.window.addEventListener('focus', refreshWhenVisible);
        globalThis.document.addEventListener('visibilitychange', refreshWhenVisible);

        return () => {
            globalThis.window.clearInterval(intervalId);
            globalThis.window.removeEventListener('focus', refreshWhenVisible);
            globalThis.document.removeEventListener('visibilitychange', refreshWhenVisible);
        };
    }, [loadSites]);

    const refresh = useCallback(async() => {
        await loadSites(true);
    }, [loadSites]);

    return { sites, isLoading, error, refresh };
}
