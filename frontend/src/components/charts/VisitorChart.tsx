import { CategoryScale, Chart as ChartJS, Filler, Legend, LinearScale, LineElement, PointElement, Tooltip } from 'chart.js';
import { Line } from 'react-chartjs-2';
import type { VisitorSeries } from '@/types/stats';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip, Legend);

const CHART_OPTIONS = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false as const,
    plugins: { legend: { position: 'bottom' as const, labels: { boxWidth: 10, font: { size: 11 } } } },
    scales: {
        y: { beginAtZero: true, ticks: { font: { size: 10 } }, grid: { color: '#f4f4f5' } },
        x: { ticks: { font: { size: 10 } }, grid: { display: false } }
    }
};

function formatLabel(value: string, interval: string): string {
    if (interval === 'minute' || interval === 'hour') {
        const match = value.match(/T(\d{2}:\d{2})/);
        if (match) {
            return match[1];
        }
    }

    return value;
}

interface VisitorChartProps {
    series: VisitorSeries;
}

export function VisitorChart({ series }: VisitorChartProps) {
    const labels = series.data.map((row) => formatLabel(row.date, series.interval));
    const chartData = {
        labels,
        datasets: [
            {
                label: 'Visitors',
                data: series.data.map((row) => row.uniqueVisitors),
                borderColor: '#2563eb',
                backgroundColor: 'rgba(37,99,235,0.08)',
                fill: true,
                tension: 0.3,
                borderWidth: 1.5,
                pointRadius: 0
            },
            {
                label: 'Pageviews',
                data: series.data.map((row) => row.pageviews),
                borderColor: '#a1a1aa',
                backgroundColor: 'rgba(161,161,170,0.06)',
                tension: 0.3,
                borderWidth: 1.5,
                pointRadius: 0
            }
        ]
    };

    return (
        <div className="rounded-lg border border-zinc-200 bg-white p-3">
            <h3 className="text-sm font-medium text-zinc-900">Visitors</h3>
            <div className="mt-2 h-56">
                { series.data.length === 0 ? (
                    <div className="flex h-full items-center justify-center text-xs text-zinc-400">
                        No data yet.
                    </div>
                ) : (
                    <Line options={ CHART_OPTIONS } data={ chartData }/>
                ) }
            </div>
        </div>
    );
}
