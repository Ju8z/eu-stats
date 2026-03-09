import apiClient from './client';
import type { DeviceStats, EventStats, GeoStats, OverviewStats, PageStats, RealTimeStats, ReferrerStats, VisitorSeries } from '../types/stats';

const base = (siteId: number) => `/sites/${ siteId }/d`;

export const statsApi = {

    overview: async(siteId: number, period = '30d'): Promise<OverviewStats> => {
        const { data } = await apiClient.get<OverviewStats>(`${ base(siteId) }/summary`, { params: { period } });
        return data;
    },

    visitors: async(siteId: number, period = '30d', interval = 'day'): Promise<VisitorSeries> => {
        const { data } = await apiClient.get<VisitorSeries>(`${ base(siteId) }/audience`, { params: { period, interval } });
        return data;
    },

    pages: async(siteId: number, period = '30d'): Promise<PageStats> => {
        const { data } = await apiClient.get<PageStats>(`${ base(siteId) }/content`, { params: { period } });
        return data;
    },

    referrers: async(siteId: number, period = '30d'): Promise<ReferrerStats> => {
        const { data } = await apiClient.get<ReferrerStats>(`${ base(siteId) }/sources`, { params: { period } });
        return data;
    },

    geo: async(siteId: number, period = '30d'): Promise<GeoStats> => {
        const { data } = await apiClient.get<GeoStats>(`${ base(siteId) }/regions`, { params: { period } });
        return data;
    },

    devices: async(siteId: number, period = '30d'): Promise<DeviceStats> => {
        const { data } = await apiClient.get<DeviceStats>(`${ base(siteId) }/tech`, { params: { period } });
        return data;
    },

    events: async(siteId: number, period = '30d'): Promise<EventStats> => {
        const { data } = await apiClient.get<EventStats>(`${ base(siteId) }/actions`, { params: { period } });
        return data;
    },

    realtime: async(siteId: number): Promise<RealTimeStats> => {
        const { data } = await apiClient.get<RealTimeStats>(`${ base(siteId) }/live`);
        return data;
    }
};
