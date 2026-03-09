export interface Site {
    id: number;
    name: string;
    domain: string;
    totalPageviewsLast30Days: number;
    uniqueVisitorsLast30Days: number;
    createdAt: string;
}

export interface SiteListResponse {
    sites: Site[];
}

export interface CreateSiteRequest {
    name: string;
    domain: string;
}

export interface UpdateSiteRequest {
    name: string;
    domain: string;
}

export interface SnippetResponse {
    snippetHtml: string;
    siteId: number;
    domain: string;
}
