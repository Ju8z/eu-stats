import { Link } from 'react-router-dom';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { EmptyState } from '@/components/common/EmptyState';
import { SiteCard } from '@/components/sites/SiteCard';
import { useSites } from '@/hooks/useSites';

export default function DashboardPage() {
    const { sites, isLoading, error, refresh } = useSites();

    if (isLoading) {
        return <LoadingSpinner/>;
    }

    if (error) {
        return (
            <div className="space-y-3">
                <h1 className="text-lg font-semibold text-zinc-900">SITES</h1>
                <div className="max-w-lg rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                    <p>{ error }</p>
                    <button
                        type="button"
                        onClick={ () => void refresh() }
                        className="mt-3 rounded bg-red-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-red-700"
                    >
                        RETRY
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <h1 className="text-lg font-semibold text-zinc-900">SITES</h1>
                <Link
                    to="/sites/new"
                    className="rounded bg-blue-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-blue-700"
                >
                    + ADD SITE
                </Link>
            </div>
            { sites.length === 0 ? (
                <EmptyState title="No sites yet" description="Create your first site to start collecting analytics."/>
            ) : (
                <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
                    { sites.map((site) => (
                        <SiteCard key={ site.id } site={ site }/>
                    )) }
                </div>
            ) }
        </div>
    );
}
