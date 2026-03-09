import React from 'react';
import { Link } from 'react-router-dom';
import type { Site } from '../../types/site';
import { formatNumber } from '../../utils/formatters';

export class SiteCard extends React.Component<{ site: Site }> {

    render() {
        return (
            <Link
                to={ `/sites/${ this.props.site.id }/analytics` }
                className="block rounded-lg border border-zinc-200 bg-white p-3 transition hover:border-zinc-300"
            >
                <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                        <h3 className="truncate text-sm font-medium text-zinc-900">{ this.props.site.name }</h3>
                        <p className="text-xs text-zinc-400">{ this.props.site.domain }</p>
                    </div>
                    <Link
                        to={ `/sites/${ this.props.site.id }/settings` }
                        onClick={ (event) => event.stopPropagation() }
                        className="shrink-0 rounded bg-zinc-100 px-1.5 py-0.5 text-[10px] font-medium text-zinc-700 hover:bg-zinc-200"
                    >
                        SETTINGS
                    </Link>
                </div>
                <div className="mt-2 flex gap-4 text-xs">
                    <div>
                        <span className="text-zinc-400">Views </span>
                        <span className="font-medium text-zinc-900">{ formatNumber(this.props.site.totalPageviewsLast30Days) }</span>
                    </div>
                    <div>
                        <span className="text-zinc-400">Visitors </span>
                        <span className="font-medium text-zinc-900">{ formatNumber(this.props.site.uniqueVisitorsLast30Days) }</span>
                    </div>
                </div>
            </Link>
        );
    }
}
