import React from 'react';

export class EmptyState extends React.Component<{ title: string; description: string }> {

    render() {
        return (
            <div className="rounded-lg border border-dashed border-zinc-300 bg-white p-6 text-center">
                <h3 className="text-sm font-medium text-zinc-900">{ this.props.title }</h3>
                <p className="mt-1 text-xs text-zinc-400">{ this.props.description }</p>
            </div>
        );
    }
}
