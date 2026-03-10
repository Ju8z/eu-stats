import { BarElement, CategoryScale, Chart as ChartJS, Legend, LinearScale, Tooltip } from 'chart.js';
import { Bar } from 'react-chartjs-2';
import type { GeoStats } from '@/types/stats';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const CHART_OPTIONS = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false as const,
    plugins: { legend: { display: false } },
    scales: {
        y: { ticks: { font: { size: 10 } }, grid: { color: '#f4f4f5' } },
        x: { ticks: { font: { size: 10 } }, grid: { display: false } }
    }
};

interface GeoBarChartProps {
    data: GeoStats;
}

export function GeoBarChart({ data }: GeoBarChartProps) {
    const top = data.data.slice(0, 10);
    const chartData = {
        labels: top.map((row) => row.countryName),
        datasets: [
            {
                label: 'Visits',
                data: top.map((row) => row.visits),
                backgroundColor: '#2563eb',
                borderRadius: 2
            }
        ]
    };

    return (
        <div className="rounded-lg border border-zinc-200 bg-white p-3">
            <h3 className="text-sm font-medium text-zinc-900">Countries</h3>
            <div className="mt-2 h-56">
                { top.length === 0 ? (
                    <div className="flex h-full items-center justify-center text-xs text-zinc-400">
                        No geo data yet.
                    </div>
                ) : (
                    <Bar options={ CHART_OPTIONS } data={ chartData }/>
                ) }
            </div>
        </div>
    );
}
