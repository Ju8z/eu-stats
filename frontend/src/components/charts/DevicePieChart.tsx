import React from 'react';
import type { DeviceStats } from '../../types/stats';
import { PieChart, type PieChartItem } from './PieChart';

function toDevicePieItems(data: DeviceStats): PieChartItem[] {
    return data.deviceTypes.map((row) => ({
        label: row.type,
        visits: row.visits
    }));
}

export class DevicePieChart extends React.PureComponent<{ data: DeviceStats }> {

    render() {
        return <PieChart title="Devices" emptyMessage="No device data yet." items={ toDevicePieItems(this.props.data) }/>;
    }
}
