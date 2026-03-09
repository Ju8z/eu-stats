import apiClient from './client';
import type { CreateSiteRequest, Site, SiteListResponse, SnippetResponse, UpdateSiteRequest } from '../types/site';

export const sitesApi = {

    list: async(): Promise<SiteListResponse> => {
        const { data } = await apiClient.get<SiteListResponse>('/sites');
        return data;
    },

    create: async(payload: CreateSiteRequest): Promise<Site> => {
        const { data } = await apiClient.post<Site>('/sites', payload);
        return data;
    },

    get: async(siteId: number): Promise<Site> => {
        const { data } = await apiClient.get<Site>(`/sites/${ siteId }`);
        return data;
    },

    update: async(siteId: number, payload: UpdateSiteRequest): Promise<Site> => {
        const { data } = await apiClient.put<Site>(`/sites/${ siteId }`, payload);
        return data;
    },

    remove: async(siteId: number): Promise<void> => {
        await apiClient.delete(`/sites/${ siteId }`);
    },

    snippet: async(siteId: number): Promise<SnippetResponse> => {
        const { data } = await apiClient.get<SnippetResponse>(`/sites/${ siteId }/snippet`);
        return data;
    }
};
