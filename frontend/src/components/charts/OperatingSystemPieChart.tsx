import React from 'react';
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

export class OperatingSystemPieChart extends React.PureComponent<{ data: DeviceStats }> {

    render() {
        return (
            <PieChart
                title="Operating Systems" emptyMessage="No operating system data yet." items={ toOperatingSystemPieItems(this.props.data) }/>);
    }
}
