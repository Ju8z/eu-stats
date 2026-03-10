import apiClient from '@/api/client';
import type { CreateSiteRequest, Site, SiteListResponse, SnippetResponse, UpdateSiteRequest } from '@/types/site';

/**
 * Centralizes site-related backend calls.
 * Keeping routes and response typing here prevents pages and hooks from hard-coding endpoint paths or
 * duplicating request details.
 */
export const sitesApi = {

    /**
     * Keeps dashboard site loading behind one typed call.
     * The wrapper hides the response envelope so hooks can focus on site state instead of transport details.
     */
    list: async(): Promise<SiteListResponse> => {
        const { data } = await apiClient.get<SiteListResponse>('/sites');
        return data;
    },

    /**
     * Keeps site creation on one explicit request path.
     * Forms can submit normalized payloads without needing to know how the backend returns the persisted site.
     */
    create: async(payload: CreateSiteRequest): Promise<Site> => {
        const { data } = await apiClient.post<Site>('/sites', payload);
        return data;
    },

    /**
     * Keeps single-site reads consistent across settings and future detail screens.
     * A shared call site prevents route fragments and response typing from drifting between pages.
     */
    get: async(siteId: number): Promise<Site> => {
        const { data } = await apiClient.get<Site>(`/sites/${ siteId }`);
        return data;
    },

    /**
     * Keeps site updates on one typed call path.
     * That lets the settings screen focus on form state instead of assembling transport details by hand.
     */
    update: async(siteId: number, payload: UpdateSiteRequest): Promise<Site> => {
        const { data } = await apiClient.put<Site>(`/sites/${ siteId }`, payload);
        return data;
    },

    /**
     * Keeps destructive site removal behind one explicit helper.
     * Confirmation stays in the UI, while the transport details remain centralized here.
     */
    remove: async(siteId: number): Promise<void> => {
        await apiClient.delete(`/sites/${ siteId }`);
    },

    /**
     * Keeps snippet retrieval separate from general site reads.
     * Installation screens can ask only for embed markup without depending on unrelated site responses.
     */
    snippet: async(siteId: number): Promise<SnippetResponse> => {
        const { data } = await apiClient.get<SnippetResponse>(`/sites/${ siteId }/snippet`);
        return data;
    }
};
