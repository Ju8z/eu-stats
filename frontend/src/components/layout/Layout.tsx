import type { ReactNode } from 'react';
import { Header } from '@/components/layout/Header';

interface LayoutProps {
    children: ReactNode;
}

/**
 * Defines the shared page chrome for routed screens.
 * Centralizing header, footer, and spacing keeps feature pages focused on content instead of repeating the
 * application shell.
 */
export function Layout({ children }: LayoutProps) {
    return (
        <div className="min-h-screen bg-zinc-50 text-zinc-900">
            <Header/>
            <main className="mx-auto max-w-6xl px-4 py-4">
                { children }
            </main>
            <footer className="mx-auto max-w-6xl px-4 pb-6">
                <p className="text-xs text-zinc-400">
                    No cookies, no raw IP storage, no fingerprinting, no cross-site tracking. Visitor identity is hashed with a daily rotating salt.
                </p>
            </footer>
        </div>
    );
}
