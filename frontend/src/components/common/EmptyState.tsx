interface EmptyStateProps {
    title: string;
    description: string;
}

/**
 * Standardizes the way empty content is presented.
 * Reusing one empty-state component keeps “no data” messaging visually consistent across the application.
 */
export function EmptyState({ title, description }: EmptyStateProps) {
    return (
        <div className="rounded-lg border border-dashed border-zinc-300 bg-white p-6 text-center">
            <h3 className="text-sm font-medium text-zinc-900">{ title }</h3>
            <p className="mt-1 text-xs text-zinc-400">{ description }</p>
        </div>
    );
}
