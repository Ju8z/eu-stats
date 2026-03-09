import React from 'react';
import { Header } from './Header';

export class Layout extends React.Component<{ children: React.ReactNode }> {

    render() {
        return (
            <div className="min-h-screen bg-zinc-50 text-zinc-900">
                <Header/>
                <main className="mx-auto max-w-6xl px-4 py-4">
                    { this.props.children }
                </main>
                <footer className="mx-auto max-w-6xl px-4 pb-6">
                    <p className="text-xs text-zinc-400">
                        No cookies, no raw IP storage, no fingerprinting, no cross-site tracking. Visitor identity is hashed with a daily rotating salt.
                    </p>
                </footer>
            </div>
        );
    }
}
