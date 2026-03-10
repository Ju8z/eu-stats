import apiClient from '@/api/client';
import type { DeviceStats, EventStats, GeoStats, OverviewStats, PageStats, RealTimeStats, ReferrerStats, VisitorSeries } from '@/types/stats';

const base = (siteId: number) => `/sites/${ siteId }/d`;

/**
 * Centralizes analytics endpoint calls.
 * Pages and hooks can stay focused on dashboard behavior because route fragments and response typing are
 * defined in one place.
 */
export const statsApi = {

    /**
     * Keeps overview reads separate from heavier dashboard sections.
     * Callers can refresh headline metrics without pulling every breakdown endpoint.
     */
    overview: async(siteId: number, period = '30d'): Promise<OverviewStats> => {
        const { data } = await apiClient.get<OverviewStats>(`${ base(siteId) }/summary`, { params: { period } });
        return data;
    },

    /**
     * Keeps interval-aware visitor chart requests on one typed path.
     * The chart code does not need to know how interval and period parameters are encoded for the backend.
     */
    visitors: async(siteId: number, period = '30d', interval = 'day'): Promise<VisitorSeries> => {
        const { data } = await apiClient.get<VisitorSeries>(`${ base(siteId) }/audience`, { params: { period, interval } });
        return data;
    },

    /**
     * Keeps page leaderboard reads separate from the rest of the dashboard.
     * That lets hooks refresh ranked content data without coupling it to other analytics sections.
     */
    pages: async(siteId: number, period = '30d'): Promise<PageStats> => {
        const { data } = await apiClient.get<PageStats>(`${ base(siteId) }/content`, { params: { period } });
        return data;
    },

    /**
     * Keeps referrer ranking requests behind one shared helper.
     * Frontend code can request source data without duplicating route fragments or response typing.
     */
    referrers: async(siteId: number, period = '30d'): Promise<ReferrerStats> => {
        const { data } = await apiClient.get<ReferrerStats>(`${ base(siteId) }/sources`, { params: { period } });
        return data;
    },

    /**
     * Keeps geographic chart reads on a dedicated helper.
     * The frontend can request only the compact country payload that the chart actually consumes.
     */
    geo: async(siteId: number, period = '30d'): Promise<GeoStats> => {
        const { data } = await apiClient.get<GeoStats>(`${ base(siteId) }/regions`, { params: { period } });
        return data;
    },

    /**
     * Keeps device breakdown reads behind one typed boundary.
     * That makes it easier to reshape or trim the device payload without touching dashboard pages.
     */
    devices: async(siteId: number, period = '30d'): Promise<DeviceStats> => {
        const { data } = await apiClient.get<DeviceStats>(`${ base(siteId) }/tech`, { params: { period } });
        return data;
    },

    /**
     * Keeps custom event analytics separate from other dashboard sections.
     * The event table can evolve independently because its transport details remain isolated here.
     */
    events: async(siteId: number, period = '30d'): Promise<EventStats> => {
        const { data } = await apiClient.get<EventStats>(`${ base(siteId) }/actions`, { params: { period } });
        return data;
    },

    /**
     * Keeps live activity polling behind one dedicated helper.
     * Historical pages do not need to know that the real-time widget is backed by a separate endpoint.
     */
    realtime: async(siteId: number): Promise<RealTimeStats> => {
        const { data } = await apiClient.get<RealTimeStats>(`${ base(siteId) }/live`);
        return data;
    }
};
