import { useCallback, useEffect, useState } from 'react';
import { sitesApi } from '../api/sitesApi';
import type { Site } from '../types/site';

export function useSites() {
    const [sites, setSites] = useState<Site[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const refresh = useCallback(async() => {
        setIsLoading(true);
        setError(null);
        try {
            const result = await sitesApi.list();
            setSites(result.sites);
        } catch (error_) {
            console.error('Failed to load sites:', error_);
            setSites([]);
            setError('Failed to load sites. Please check if backend and database are running.');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        void refresh();
    }, [refresh]);

    return { sites, isLoading, error, refresh };
}
