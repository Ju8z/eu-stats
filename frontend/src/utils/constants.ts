/**
 * Collects the supported analytics periods in one place.
 * Centralizing the allowed values keeps the picker and the hooks aligned on which range strings the backend
 * understands.
 */
export const PERIOD_OPTIONS = [
    { value: '1h', label: 'Last Hour' },
    { value: 'today', label: 'Today' },
    { value: '7d', label: 'Last 7 Days' },
    { value: '30d', label: 'Last 30 Days' }
];
