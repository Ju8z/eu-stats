import type { RealTimeStats } from '@/types/stats';

interface RealTimeWidgetProps {
    data: RealTimeStats | null;
}

/**
 * Displays the live activity snapshot separately from historical charts.
 * Keeping the heartbeat-driven view isolated makes it easier to refresh or replace without affecting the
 * rest of the analytics dashboard.
 */
export function RealTimeWidget({ data }: RealTimeWidgetProps) {
    const activePages = data?.topActivePages ?? [];
    const activeVisitors = data?.activeVisitors ?? 0;
    const activeTabs = data?.activeTabs ?? activeVisitors;

    return (
        <aside className="rounded-lg border border-zinc-200 bg-white p-3">
            <h3 className="text-sm font-medium text-zinc-900">Live</h3>
            <p className="mt-0.5 text-2xl font-semibold text-blue-600">{ activeTabs }</p>
            <p className="text-[10px] font-medium uppercase tracking-wide text-zinc-400">active tabs</p>
            <p className="mt-1 text-xs text-zinc-500">
                { activeVisitors } unique { activeVisitors === 1 ? 'visitor' : 'visitors' }
            </p>
            <ul className="mt-2 space-y-1 text-xs">
                { activePages.map((page) => (
                    <li key={ page.url } className="flex items-center justify-between rounded bg-zinc-50 px-2 py-1">
                        <span className="truncate text-zinc-600">{ page.url }</span>
                        <span className="ml-2 shrink-0 font-medium text-zinc-900">{ page.activeTabs ?? page.visitors ?? 0 }</span>
                    </li>
                )) }
            </ul>
            { activePages.length === 0 && (
                <p className="mt-2 text-xs text-zinc-400">No active pages.</p>
            ) }
        </aside>
    );
}
