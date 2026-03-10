import type { DeviceStats } from '@/types/stats';
import { PieChart, type PieChartItem } from '@/components/charts/PieChart';

function toDevicePieItems(data: DeviceStats): PieChartItem[] {
    return data.deviceTypes.map((row) => ({
        label: row.type,
        visits: row.visits
    }));
}

interface DevicePieChartProps {
    data: DeviceStats;
}

export function DevicePieChart({ data }: DevicePieChartProps) {
    return <PieChart title="Devices" emptyMessage="No device data yet." items={ toDevicePieItems(data) }/>;
}
