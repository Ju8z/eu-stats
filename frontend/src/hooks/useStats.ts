import { useCallback, useEffect, useRef, useState } from 'react';
import { statsApi } from '../api/statsApi';
import type { DeviceStats, EventStats, GeoStats, OverviewStats, PageStats, RealTimeStats, ReferrerStats, VisitorSeries } from '../types/stats';

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

function hasSameData<T>(current: T, incoming: T): boolean {
    if (current === incoming) {
        return true;
    }
    if (current == null || incoming == null) {
        return false;
    }
    try {
        return JSON.stringify(current) === JSON.stringify(incoming);
    } catch (error_) {
        console.error('JSON.stringify error in hasSameData:', error_);
        return false;
    }
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
        const generalPeriod = period === '1h' ? 'today' : period;

        let visitorsInterval = 'day';
        if (period === '1h') {
            visitorsInterval = 'minute';
        } else if (period === 'today') {
            visitorsInterval = 'hour';
        }

        try {
            const results = await Promise.allSettled([
                statsApi.overview(siteId, generalPeriod),
                statsApi.visitors(siteId, period, visitorsInterval),
                statsApi.pages(siteId, generalPeriod),
                statsApi.referrers(siteId, generalPeriod),
                statsApi.geo(siteId, generalPeriod),
                statsApi.devices(siteId, generalPeriod),
                statsApi.events(siteId, generalPeriod)
            ]);

            setStats((prev) => {
                let changed = false;
                const next: SiteStatsBundle = { ...prev };

                if (results[0].status === 'fulfilled' && !hasSameData(prev.overview, results[0].value)) {
                    next.overview = results[0].value;
                    changed = true;
                }
                if (results[1].status === 'fulfilled' && !hasSameData(prev.visitors, results[1].value)) {
                    next.visitors = results[1].value;
                    changed = true;
                }
                if (results[2].status === 'fulfilled' && !hasSameData(prev.pages, results[2].value)) {
                    next.pages = results[2].value;
                    changed = true;
                }
                if (results[3].status === 'fulfilled' && !hasSameData(prev.referrers, results[3].value)) {
                    next.referrers = results[3].value;
                    changed = true;
                }
                if (results[4].status === 'fulfilled' && !hasSameData(prev.geo, results[4].value)) {
                    next.geo = results[4].value;
                    changed = true;
                }
                if (results[5].status === 'fulfilled' && !hasSameData(prev.devices, results[5].value)) {
                    next.devices = results[5].value;
                    changed = true;
                }
                if (results[6].status === 'fulfilled' && !hasSameData(prev.events, results[6].value)) {
                    next.events = results[6].value;
                    changed = true;
                }

                return changed ? next : prev;
            });
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
            setStats((prev) =>
                hasSameData(prev.realtime, realtime)
                    ? prev
                    : { ...prev, realtime }
            );
        } catch (error_) {
            // Keep previous realtime snapshot if a single poll fails, just in case..
            console.error('Failed to refresh realtime stats:', error_);
        }
    }, [siteId]);

    const refresh = useCallback(async() => {
        await Promise.all([refreshAll(false), refreshRealtime()]);
    }, [refreshAll, refreshRealtime]);

    useEffect(() => {
        const showLoading = !hasLoadedRef.current;
        void refreshAll(showLoading);
        void refreshRealtime();

        const statsInterval = globalThis.window.setInterval(() => {
            void refreshAll(false);
        }, 10000);

        const realtimeInterval = globalThis.window.setInterval(() => {
            void refreshRealtime();
        }, 3000);

        return () => {
            globalThis.window.clearInterval(statsInterval);
            globalThis.window.clearInterval(realtimeInterval);
        };
    }, [refreshAll, refreshRealtime]);

    return { ...stats, isLoading, refresh };
}
