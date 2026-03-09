import React from 'react';
import { ArcElement, Chart as ChartJS, Legend, Tooltip } from 'chart.js';
import { Pie } from 'react-chartjs-2';
import type { DeviceStats } from '../../types/stats';

ChartJS.register(ArcElement, Tooltip, Legend);

const CHART_OPTIONS = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false as const,
    plugins: { legend: { position: 'bottom' as const, labels: { boxWidth: 10, font: { size: 11 } } } }
};

export class DevicePieChart extends React.PureComponent<{ data: DeviceStats }> {

    render() {
        const chartData = {
            labels: this.props.data.deviceTypes.map((row) => row.type),
            datasets: [
                {
                    data: this.props.data.deviceTypes.map((row) => row.visits),
                    backgroundColor: ['#2563eb', '#18181b', '#a1a1aa', '#d4d4d8']
                }
            ]
        };

        return (
            <div className="rounded-lg border border-zinc-200 bg-white p-3">
                <h3 className="text-sm font-medium text-zinc-900">Devices</h3>
                <div className="mt-2 h-56">
                    { this.props.data.deviceTypes.length === 0 ? (
                        <div className="flex h-full items-center justify-center text-xs text-zinc-400">
                            No device data yet.
                        </div>
                    ) : (
                        <Pie options={ CHART_OPTIONS } data={ chartData }/>
                    ) }
                </div>
            </div>
        );
    }
}
