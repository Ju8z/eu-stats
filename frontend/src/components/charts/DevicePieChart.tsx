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

/**
 * Adapts device analytics to the shared pie chart input.
 * Keeping the mapping here prevents the generic chart from depending on device-specific field names.
 */
export function DevicePieChart({ data }: DevicePieChartProps) {
    return <PieChart title="Devices" emptyMessage="No device data yet." items={ toDevicePieItems(data) }/>;
}
