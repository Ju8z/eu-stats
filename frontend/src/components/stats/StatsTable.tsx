import type { ReactNode } from 'react';

interface Column<T> {
    key: string;
    title: string;
    render: (row: T) => ReactNode;
}

interface StatsTableProps<T> {
    columns: Column<T>[];
    rows: T[];
}

/**
 * Provides one small table primitive for analytics lists.
 * The render-based column model keeps domain tables flexible without forcing each screen to duplicate table
 * structure and empty-state handling.
 */
export function StatsTable<T extends Record<string, unknown>>({ columns, rows }: StatsTableProps<T>) {
    return (
        <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
            <table className="min-w-full text-xs">
                <thead className="bg-zinc-50">
                    <tr>
                        { columns.map((column) => (
                            <th key={ column.key } className="px-3 py-2 text-left text-[11px] font-medium uppercase tracking-wide text-zinc-400">
                                { column.title }
                            </th>
                        )) }
                    </tr>
                </thead>
                <tbody className="divide-y divide-zinc-100">
                { rows.length === 0 ? (
                        <tr>
                            <td colSpan={ columns.length } className="px-3 py-3 text-center text-zinc-400">
                                No data for this period.
                            </td>
                        </tr>
                    ) : (
                    rows.map((row, index) => (
                            <tr key={ index } className="hover:bg-zinc-50">
                                { columns.map((column) => (
                                    <td key={ column.key } className="px-3 py-1.5 text-zinc-700">
                                        { column.render(row) }
                                    </td>
                                )) }
                            </tr>
                        ))
                    ) }
                </tbody>
            </table>
        </div>
    );
}
