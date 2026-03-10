import { ArcElement, Chart as ChartJS, Legend, Tooltip } from 'chart.js';
import { Pie } from 'react-chartjs-2';

ChartJS.register(ArcElement, Tooltip, Legend);

const PIE_COLORS = [
    '#2563eb',
    '#18181b',
    '#a1a1aa',
    '#d4d4d8',
    '#0f766e',
    '#f59e0b',
    '#dc2626',
    '#7c3aed'
];

const CHART_OPTIONS = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false as const,
    plugins: { legend: { position: 'bottom' as const, labels: { boxWidth: 10, font: { size: 11 } } } }
};

/**
 * Defines the minimal input shape shared pie charts need.
 * Keeping this contract small lets domain-specific chart wrappers translate richer analytics data without
 * coupling the generic chart to backend response types.
 */
export interface PieChartItem {
    label: string;
    visits: number;
}

interface PieChartProps {
    title: string;
    emptyMessage: string;
    items: PieChartItem[];
}

/**
 * Renders a reusable pie chart from already-translated input data.
 * The chart stays generic on purpose so analytics-specific mapping logic can live in small wrapper
 * components instead of inside the chart itself.
 */
export function PieChart({ title, emptyMessage, items }: PieChartProps) {
    const chartData = {
        labels: items.map((item) => item.label),
        datasets: [
            {
                data: items.map((item) => item.visits),
                backgroundColor: items.map((_, index) => PIE_COLORS[index % PIE_COLORS.length])
            }
        ]
    };

    return (
        <div className="rounded-lg border border-zinc-200 bg-white p-3">
            <h3 className="text-sm font-medium text-zinc-900">{ title }</h3>
            <div className="mt-2 h-56">
                { items.length === 0 ? (
                    <div className="flex h-full items-center justify-center text-xs text-zinc-400">
                        { emptyMessage }
                    </div>
                ) : (
                    <Pie options={ CHART_OPTIONS } data={ chartData }/>
                ) }
            </div>
        </div>
    );
}
