import { Link } from 'react-router-dom';
import type { Site } from '@/types/site';
import { formatNumber } from '@/utils/formatters';

interface SiteCardProps {
    site: Site;
}

/**
 * Condenses a site summary into a reusable dashboard card.
 * The card keeps navigation and thirty-day metrics together so the site list stays simple and scannable.
 */
export function SiteCard({ site }: SiteCardProps) {
    return (
        <article className="relative rounded-lg border border-zinc-200 bg-white p-3 transition hover:border-zinc-300">
            <Link
                to={ `/sites/${ site.id }/analytics` }
                aria-label={ `Open analytics for ${ site.name }` }
                className="absolute inset-0 rounded-lg"
            />
            <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                    <h3 className="truncate text-sm font-medium text-zinc-900">{ site.name }</h3>
                    <p className="text-xs text-zinc-400">{ site.domain }</p>
                </div>
                <Link
                    to={ `/sites/${ site.id }/settings` }
                    className="relative z-10 shrink-0 rounded bg-zinc-100 px-1.5 py-0.5 text-[10px] font-medium text-zinc-700 hover:bg-zinc-200"
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
        </article>
    );
}
