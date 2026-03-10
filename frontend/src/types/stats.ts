/**
 * Defines the summary metrics that drive the overview cards.
 * Keeping comparison values inside the same contract prevents card rendering from having to coordinate
 * separate totals and trend responses.
 */
export interface OverviewStats {
    totalPageviews: number;
    uniqueVisitors: number;
    comparison: {
        pageviewsChange: number;
        visitorsChange: number;
    };
}

/**
 * Defines chart-ready visitor series data.
 * Treating the interval and rows as one contract lets chart components format labels without guessing the
 * bucket size from the selected filter alone.
 */
export interface VisitorSeries {
    interval: string;
    data: Array<{
        date: string;
        uniqueVisitors: number;
        pageviews: number;
    }>;
}

/**
 * Defines the ranked page rows shown in the content table.
 * Keeping the table contract separate from the full dashboard state makes it easier to replace or reorder
 * tables without changing unrelated components.
 */
export interface PageStats {
    data: Array<{
        url: string;
        title: string;
        pageviews: number;
        uniqueVisitors: number;
    }>;
}

/**
 * Defines the ranked referrer rows shown in the traffic-source table.
 * The dedicated contract keeps category labeling close to the data that uses it instead of pushing that
 * responsibility into generic table code.
 */
export interface ReferrerStats {
    data: Array<{
        referrer: string;
        category: string;
        visits: number;
        uniqueVisitors: number;
    }>;
}

/**
 * Defines the compact geographic payload used by the countries chart.
 * The chart only needs display labels and visit counts, so the contract stays intentionally smaller than the
 * backend aggregation source.
 */
export interface GeoStats {
    data: Array<{
        countryName: string;
        visits: number;
    }>;
}

/**
 * Defines the device and operating-system slices used by the dashboard charts.
 * Keeping this shape focused on rendered chart inputs avoids coupling the frontend to backend aggregation
 * detail that is not currently visualized.
 */
export interface DeviceStats {
    deviceTypes: Array<{ type: string; visits: number }>;
    operatingSystems: Array<{ name: string; version: string; visits: number }>;
}

/**
 * Defines the event rows rendered in the custom events table.
 * A dedicated contract keeps event reporting free to evolve without affecting other analytics sections.
 */
export interface EventStats {
    data: Array<{
        eventName: string;
        count: number;
        uniqueVisitors: number;
    }>;
}

/**
 * Defines the live snapshot shown by the real-time widget.
 * Keeping live data separate from historical statistics lets the UI reflect heartbeat-driven updates without
 * mixing that behavior into the historical chart contracts.
 */
export interface RealTimeStats {
    activeVisitors: number;
    topActivePages: Array<{ url: string; visitors: number }>;
}
