import { PERIOD_OPTIONS } from '@/utils/constants';

interface DateRangePickerProps {
    value: string;
    onChange: (value: string) => void;
}

/**
 * Restricts analytics filtering to the periods the backend supports.
 * Centralizing the picker prevents screens from inventing unsupported range values or label variants.
 */
export function DateRangePicker({ value, onChange }: DateRangePickerProps) {
    return (
        <select
            value={ value }
            onChange={ (event) => onChange(event.target.value) }
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
