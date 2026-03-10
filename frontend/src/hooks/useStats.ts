import axios from 'axios';
import { useCallback, useEffect, useRef, useState } from 'react';
import { statsApi } from '@/api/statsApi';
import type { DeviceStats, EventStats, GeoStats, OverviewStats, PageStats, RealTimeStats, ReferrerStats, VisitorSeries } from '@/types/stats';

interface SiteStatsBundle {
    overview: OverviewStats | null;
    visitors: VisitorSeries | null;
    pages: PageStats | null;
    referrers: ReferrerStats | null;
    geo: GeoStats | null;
    devices: DeviceStats | null;
    events: EventStats | null;
    realtime: RealTimeStats | null;
}

const emptyBundle: SiteStatsBundle = {
    overview: null,
    visitors: null,
    pages: null,
    referrers: null,
    geo: null,
    devices: null,
    events: null,
    realtime: null
};

const REFRESH_INTERVAL_MILLIS = 3000;
const INVALID_SITE_MESSAGE = 'Open analytics from the dashboard so the page uses a valid site id.';

function toGeneralPeriod(period: string): string {
    if (period === '1h') {
        return 'today';
    }
    return period;
}

function toVisitorsInterval(period: string): string {
    if (period === '1h') {
        return 'minute';
    }
    if (period === 'today') {
        return 'hour';
    }

    return 'day';
}

function toStatsLoadErrorMessage(error: unknown, siteId: number): string {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
        return `Site ${ siteId } was not found. Open the dashboard and choose an existing site.`;
    }

    return 'Failed to load analytics. Please check if backend and database are running.';
}

function firstRejectedReason(results: PromiseSettledResult<unknown>[]): unknown {
    const rejectedResult = results.find((result) => result.status === 'rejected');
    return rejectedResult?.status === 'rejected' ? rejectedResult.reason : null;
}

/**
 * Centralizes analytics polling and partial-refresh behavior for one site.
 * The hook keeps dashboard pages from coordinating multiple endpoints and preserves the last successful
 * section data when one request fails.
 */
export function useStats(siteId: number, period = '30d') {
    const [stats, setStats] = useState<SiteStatsBundle>(emptyBundle);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const hasLoadedRef = useRef(false);

    const refreshAll = useCallback(async(showLoading: boolean) => {
        if (!Number.isFinite(siteId) || siteId <= 0) {
            setError(INVALID_SITE_MESSAGE);
            setIsLoading(false);
            return;
        }
        if (!showLoading && !hasLoadedRef.current && error) {
            return;
        }
        if (showLoading) {
            setIsLoading(true);
        }
        const generalPeriod = toGeneralPeriod(period);
        const visitorsInterval = toVisitorsInterval(period);

        try {
            const [
                overviewResult,
                visitorsResult,
                pagesResult,
                referrersResult,
                geoResult,
                devicesResult,
                eventsResult
            ] = await Promise.allSettled([
                statsApi.overview(siteId, generalPeriod),
                statsApi.visitors(siteId, period, visitorsInterval),
                statsApi.pages(siteId, generalPeriod),
                statsApi.referrers(siteId, generalPeriod),
                statsApi.geo(siteId, generalPeriod),
                statsApi.devices(siteId, generalPeriod),
                statsApi.events(siteId, generalPeriod)
            ]);

            const results = [
                overviewResult,
                visitorsResult,
                pagesResult,
                referrersResult,
                geoResult,
                devicesResult,
                eventsResult
            ];
            const hasSuccessfulResult = results.some((result) => result.status === 'fulfilled');

            if (!hasSuccessfulResult) {
                if (!hasLoadedRef.current) {
                    setError(toStatsLoadErrorMessage(firstRejectedReason(results), siteId));
                }
                return;
            }

            setError(null);
            setStats((previous) => ({
                ...previous,
                overview: overviewResult.status === 'fulfilled' ? overviewResult.value : previous.overview,
                visitors: visitorsResult.status === 'fulfilled' ? visitorsResult.value : previous.visitors,
                pages: pagesResult.status === 'fulfilled' ? pagesResult.value : previous.pages,
                referrers: referrersResult.status === 'fulfilled' ? referrersResult.value : previous.referrers,
                geo: geoResult.status === 'fulfilled' ? geoResult.value : previous.geo,
                devices: devicesResult.status === 'fulfilled' ? devicesResult.value : previous.devices,
                events: eventsResult.status === 'fulfilled' ? eventsResult.value : previous.events
            }));
            hasLoadedRef.current = true;
        } finally {
            if (showLoading) {
                setIsLoading(false);
            }
        }
    }, [error, period, siteId]);

    const refreshRealtime = useCallback(async() => {
        if (!Number.isFinite(siteId) || siteId <= 0) {
            return;
        }
        if (!hasLoadedRef.current) {
            return;
        }
        try {
            const realtime = await statsApi.realtime(siteId);
            setStats((previous) => ({ ...previous, realtime }));
        } catch (error_) {
            // Keep previous realtime snapshot if a single poll fails, just in case..
            console.error('Failed to refresh realtime stats:', error_);
        }
    }, [siteId]);

    useEffect(() => {
        const showLoading = !hasLoadedRef.current;
        void refreshAll(showLoading);
        void refreshRealtime();

        const statsInterval = globalThis.window.setInterval(() => {
            void refreshAll(false);
        }, REFRESH_INTERVAL_MILLIS);

        const realtimeInterval = globalThis.window.setInterval(() => {
            void refreshRealtime();
        }, REFRESH_INTERVAL_MILLIS);

        return () => {
            globalThis.window.clearInterval(statsInterval);
            globalThis.window.clearInterval(realtimeInterval);
        };
    }, [refreshAll, refreshRealtime]);

    const refresh = useCallback(async() => {
        await refreshAll(true);
        await refreshRealtime();
    }, [refreshAll, refreshRealtime]);

    return { ...stats, error, isLoading, refresh };
}
