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

export function useStats(siteId: number, period = '30d') {
    const [stats, setStats] = useState<SiteStatsBundle>(emptyBundle);
    const [isLoading, setIsLoading] = useState(true);
    const hasLoadedRef = useRef(false);

    const refreshAll = useCallback(async(showLoading: boolean) => {
        if (!Number.isFinite(siteId) || siteId <= 0) {
            setIsLoading(false);
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
    }, [period, siteId]);

    const refreshRealtime = useCallback(async() => {
        if (!Number.isFinite(siteId) || siteId <= 0) {
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

    const refresh = useCallback(async() => {
        await refreshAll(false);
        await refreshRealtime();
    }, [refreshAll, refreshRealtime]);

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

    return { ...stats, isLoading, refresh };
}
