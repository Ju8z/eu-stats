import type { DeviceStats } from '@/types/stats';
import { PieChart, type PieChartItem } from '@/components/charts/PieChart';

function formatTechnologyLabel(name: string, version: string): string {
    return version ? `${ name } ${ version }` : name;
}

function toOperatingSystemPieItems(data: DeviceStats): PieChartItem[] {
    return data.operatingSystems.map((row) => ({
        label: formatTechnologyLabel(row.name, row.version),
        visits: row.visits
    }));
}

interface OperatingSystemPieChartProps {
    data: DeviceStats;
}

export function OperatingSystemPieChart({ data }: OperatingSystemPieChartProps) {
    return (
        <PieChart
            title="Operating Systems"
            emptyMessage="No operating system data yet."
            items={ toOperatingSystemPieItems(data) }
        />
    );
}
