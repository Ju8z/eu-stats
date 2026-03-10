import { Link } from 'react-router-dom';
import type { Site } from '@/types/site';
import { formatNumber } from '@/utils/formatters';

interface SiteCardProps {
    site: Site;
}

export function SiteCard({ site }: SiteCardProps) {
    return (
        <Link
            to={ `/sites/${ site.id }/analytics` }
            className="block rounded-lg border border-zinc-200 bg-white p-3 transition hover:border-zinc-300"
        >
            <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                    <h3 className="truncate text-sm font-medium text-zinc-900">{ site.name }</h3>
                    <p className="text-xs text-zinc-400">{ site.domain }</p>
                </div>
                <Link
                    to={ `/sites/${ site.id }/settings` }
                    onClick={ (event) => event.stopPropagation() }
                    className="shrink-0 rounded bg-zinc-100 px-1.5 py-0.5 text-[10px] font-medium text-zinc-700 hover:bg-zinc-200"
                >
                    SETTINGS
                </Link>
            </div>
            <div className="mt-2 flex gap-4 text-xs">
                <div>
                    <span className="text-zinc-400">Views </span>
                    <span className="font-medium text-zinc-900">{ formatNumber(site.totalPageviewsLast30Days) }</span>
                </div>
                <div>
                    <span className="text-zinc-400">Visitors </span>
                    <span className="font-medium text-zinc-900">{ formatNumber(site.uniqueVisitorsLast30Days) }</span>
                </div>
            </div>
        </Link>
    );
}
