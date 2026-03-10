/**
 * Defines the site shape once it reaches the frontend.
 * Keeping the dashboard on one typed site contract prevents listing, creation, and settings views from
 * drifting on which site fields are considered stable.
 */
export interface Site {
    id: number;
    name: string;
    domain: string;
    totalPageviewsLast30Days: number;
    uniqueVisitorsLast30Days: number;
}

/**
 * Keeps the list envelope explicit at the frontend boundary.
 * Even though the payload is small today, preserving the wrapper avoids spreading assumptions that the
 * sites endpoint can only ever return a bare array.
 */
export interface SiteListResponse {
    sites: Site[];
}

/**
 * Defines the payload sent when a new site is created.
 * Separating creation input from the full site shape prevents forms from depending on server-generated
 * fields that do not belong in client state.
 */
export interface CreateSiteRequest {
    name: string;
    domain: string;
}

/**
 * Defines the payload sent when an existing site is updated.
 * Keeping update input narrow makes it obvious which fields the settings screen is allowed to change.
 */
export interface UpdateSiteRequest {
    name: string;
    domain: string;
}

/**
 * Defines the snippet payload the frontend expects back from the backend.
 * Isolating snippet data from the broader site contract keeps installation views decoupled from unrelated
 * site metadata.
 */
export interface SnippetResponse {
    snippetHtml: string;
}
