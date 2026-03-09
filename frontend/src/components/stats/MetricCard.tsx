import React from 'react';

export class MetricCard extends React.Component<{ title: string; value: string; change?: number }> {

    render() {
        return (
            <article className="rounded-lg border border-zinc-200 bg-white p-3">
                <p className="text-[11px] font-medium uppercase tracking-wide text-zinc-400">{ this.props.title }</p>
                <p className="mt-0.5 text-lg font-semibold text-zinc-900">{ this.props.value }</p>
                { typeof this.props.change === 'number' && (
                    <p className={ `text-xs font-medium ${ this.props.change >= 0 ? 'text-green-600' : 'text-red-600' }` }>
                        { this.props.change >= 0 ? '+' : '' }{ this.props.change.toFixed(1) }%
                    </p>
                ) }
            </article>
        );
    }
}
