import React from 'react';

type CopyButtonState = {
    copied: boolean;
};

export class CopyButton extends React.Component<{ link: string }, CopyButtonState> {

    state: CopyButtonState = { copied: false };

    render() {
        return (
            <button
                type="button"
                onClick={ this.onCopy }
                className="rounded bg-zinc-900 px-2.5 py-1 text-xs font-medium text-white transition hover:bg-zinc-700"
            >
                { this.state.copied ? 'COPIED!' : 'COPY' }
            </button>
        );
    }

    private onCopy = async() => {
        await navigator.clipboard.writeText(this.props.link);
        this.setState({ copied: true });
        globalThis.window.setTimeout(() => this.setState({ copied: false }), 1200);
    };
}
