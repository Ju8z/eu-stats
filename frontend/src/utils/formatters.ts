/**
 * Keeps number formatting consistent across cards, tables, and charts.
 * Centralizing the locale decision makes presentation changes possible without touching every analytics
 * component.
 */
export const formatNumber = (value: number): string =>
    new Intl.NumberFormat('de-DE').format(value ?? 0);
