import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { EmptyState } from '@/components/common/EmptyState';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { DevicePieChart } from '@/components/charts/DevicePieChart';
import { GeoBarChart } from '@/components/charts/GeoBarChart';
import { OperatingSystemPieChart } from '@/components/charts/OperatingSystemPieChart';
import { RealTimeWidget } from '@/components/charts/RealTimeWidget';
import { VisitorChart } from '@/components/charts/VisitorChart';
import { DateRangePicker } from '@/components/stats/DateRangePicker';
import { MetricCard } from '@/components/stats/MetricCard';
import { StatsTable } from '@/components/stats/StatsTable';
import { useStats } from '@/hooks/useStats';
import { formatNumber } from '@/utils/formatters';

/**
 * Composes the analytics dashboard from smaller cards and charts.
 * Keeping endpoint results at the page level lets presentational widgets stay simple and unaware of the
 * broader polling strategy.
 */
export default function SiteAnalyticsPage() {
    const { siteId } = useParams();
    const numericSiteId = Number(siteId);
    const [period, setPeriod] = useState('30d');
    const stats = useStats(numericSiteId, period);
    const overviewCards = stats.overview ? [
        { title: 'Pageviews', value: formatNumber(stats.overview.totalPageviews), change: stats.overview.comparison.pageviewsChange },
        { title: 'Visitors', value: formatNumber(stats.overview.uniqueVisitors), change: stats.overview.comparison.visitorsChange }
    ] : [];

    if (stats.isLoading) {
        return <LoadingSpinner/>;
    }

    if (stats.error) {
        return (
            <div className="space-y-3">
                <div className="flex items-center justify-between">
                    <h1 className="text-lg font-semibold text-zinc-900">ANALYTICS</h1>
                    <DateRangePicker value={ period } onChange={ setPeriod }/>
                </div>
                <div className="max-w-lg rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                    <p>{ stats.error }</p>
                    <div className="mt-3 flex gap-2">
                        <button
                            type="button"
                            onClick={ () => void stats.refresh() }
                            className="rounded bg-red-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-red-700"
                        >
                            RETRY
                        </button>
                        <Link
                            to="/dashboard"
                            className="rounded border border-red-200 bg-white px-3 py-1.5 text-xs font-medium text-red-700 hover:bg-red-100"
                        >
                            DASHBOARD
                        </Link>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <h1 className="text-lg font-semibold text-zinc-900">ANALYTICS</h1>
                <DateRangePicker value={ period } onChange={ setPeriod }/>
            </div>

            { overviewCards.length > 0 && (
                <section className="grid gap-2 md:grid-cols-5">
                    <div className="md:col-span-3">
                        <RealTimeWidget data={ stats.realtime }/>
                    </div>
                    <div className="grid gap-2 md:col-span-2">
                        { overviewCards.map((card) => (
                            <MetricCard key={ card.title } title={ card.title } value={ card.value } change={ card.change }/>
                        )) }
                    </div>
                </section>
            ) }

            { stats.visitors && <VisitorChart series={ stats.visitors }/> }

            { (stats.geo || stats.devices) && (
                <section className="grid gap-2 md:grid-cols-2 xl:grid-cols-3">
                    { stats.geo && <GeoBarChart data={ stats.geo }/> }
                    { stats.devices && <DevicePieChart data={ stats.devices }/> }
                    { stats.devices && <OperatingSystemPieChart data={ stats.devices }/> }
                </section>
            ) }

            { stats.overview && stats.overview.totalPageviews === 0 && (
                <EmptyState
                    title="No analytics yet"
                    description="Open the demo pages with the correct ?siteId= value or add the tracker snippet to your site."
                />
            ) }

            <section className="space-y-3">
                { stats.pages && (
                    <div>
                        <h2 className="mb-1.5 text-sm font-medium text-zinc-900">Top Pages</h2>
                        <StatsTable
                            rows={ stats.pages.data }
                            columns={ [
                                { key: 'url', title: 'URL', render: (row) => row.url },
                                { key: 'title', title: 'Title', render: (row) => row.title },
                                { key: 'views', title: 'Views', render: (row) => formatNumber(row.pageviews) },
                                { key: 'visitors', title: 'Visitors', render: (row) => formatNumber(row.uniqueVisitors) }
                            ] }
                        />
                    </div>
                ) }

                { stats.referrers && (
                    <div>
                        <h2 className="mb-1.5 text-sm font-medium text-zinc-900">Referrers</h2>
                        <StatsTable
                            rows={ stats.referrers.data }
                            columns={ [
                                { key: 'referrer', title: 'Referrer', render: (row) => row.referrer },
                                {
                                    key: 'category',
                                    title: 'Category',
                                    render: (row) => (
                                        <span className="rounded bg-zinc-100 px-1.5 py-0.5 text-[10px] font-medium text-zinc-600">
                      { row.category }
                    </span>
                                    )
                                },
                                { key: 'visits', title: 'Visits', render: (row) => formatNumber(row.visits) },
                                { key: 'visitors', title: 'Visitors', render: (row) => formatNumber(row.uniqueVisitors) }
                            ] }
                        />
                    </div>
                ) }

                { stats.events && (
                    <div>
                        <h2 className="mb-1.5 text-sm font-medium text-zinc-900">Events</h2>
                        <StatsTable
                            rows={ stats.events.data }
                            columns={ [
                                { key: 'eventName', title: 'Event', render: (row) => row.eventName },
                                { key: 'count', title: 'Count', render: (row) => formatNumber(row.count) },
                                { key: 'visitors', title: 'Visitors', render: (row) => formatNumber(row.uniqueVisitors) }
                            ] }
                        />
                    </div>
                ) }
            </section>
        </div>
    );
}
