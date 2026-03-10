interface MetricCardProps {
    title: string;
    value: string;
    change?: number;
}

/**
 * Encapsulates the repeated headline metric pattern.
 * A shared card keeps trend coloring and spacing rules consistent across the top-level dashboard summary.
 */
export function MetricCard({ title, value, change }: MetricCardProps) {
    return (
        <article className="rounded-lg border border-zinc-200 bg-white p-3">
            <p className="text-[11px] font-medium uppercase tracking-wide text-zinc-400">{ title }</p>
            <p className="mt-0.5 text-lg font-semibold text-zinc-900">{ value }</p>
            { typeof change === 'number' && (
                <p className={ `text-xs font-medium ${ change >= 0 ? 'text-green-600' : 'text-red-600' }` }>
                    { change >= 0 ? '+' : '' }{ change.toFixed(1) }%
                </p>
            ) }
        </article>
    );
}
