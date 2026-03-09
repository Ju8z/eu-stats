import React from 'react';
import { PERIOD_OPTIONS } from '../../utils/constants';

export class DateRangePicker extends React.Component<{ value: string; onChange: (value: string) => void }> {

    render() {
        return (
            <select
                value={ this.props.value }
                onChange={ (event) => this.props.onChange(event.target.value) }
                className="rounded border border-zinc-200 bg-white px-2 py-1 text-xs text-zinc-700"
            >
                { PERIOD_OPTIONS.map((option) => (
                    <option key={ option.value } value={ option.value }>
                        { option.label }
                    </option>
                )) }
            </select>
        );
    }
}
