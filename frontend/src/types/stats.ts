export interface OverviewStats {
    period: { from: string; to: string };
    totalPageviews: number;
    uniqueVisitors: number;
    topPage: string;
    topReferrer: string;
    topCountry: string;
    comparison: {
        pageviewsChange: number;
        visitorsChange: number;
    };
}

export interface VisitorSeries {
    interval: string;
    data: Array<{
        date: string;
        uniqueVisitors: number;
        pageviews: number;
    }>;
}

export interface PageStats {
    data: Array<{
        url: string;
        title: string;
        pageviews: number;
        uniqueVisitors: number;
    }>;
}

export interface ReferrerStats {
    data: Array<{
        referrer: string;
        category: string;
        visits: number;
        uniqueVisitors: number;
    }>;
}

export interface GeoStats {
    data: Array<{
        country: string;
        countryName: string;
        visits: number;
        uniqueVisitors: number;
        percentage: number;
    }>;
}

export interface DeviceStats {
    deviceTypes: Array<{ type: string; visits: number; percentage: number }>;
    browsers: Array<{ name: string; version: string; visits: number; percentage: number }>;
    operatingSystems: Array<{ name: string; version: string; visits: number; percentage: number }>;
    screenResolutions: Array<{ resolution: string; visits: number; percentage: number }>;
}

export interface EventStats {
    data: Array<{
        eventName: string;
        count: number;
        uniqueVisitors: number;
    }>;
}

export interface RealTimeStats {
    activeVisitors: number;
    windowMinutes: number;
    topActivePages: Array<{ url: string; visitors: number }>;
}
