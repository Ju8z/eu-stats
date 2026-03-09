export const formatNumber = (value: number): string =>
    new Intl.NumberFormat('de-DE').format(value ?? 0);
