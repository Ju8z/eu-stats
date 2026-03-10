import { useEffect, useRef, useState } from 'react';

interface CopyButtonProps {
    link: string;
}

/**
 * Encapsulates clipboard copying behind one small component.
 * Keeping timeout cleanup and clipboard interaction here prevents snippet views from each re-implementing
 * browser-specific copy behavior.
 */
export function CopyButton({ link }: CopyButtonProps) {
    const [copied, setCopied] = useState(false);
    const timeoutRef = useRef<number | null>(null);

    useEffect(() => {
        return () => {
            if (timeoutRef.current !== null) {
                globalThis.window.clearTimeout(timeoutRef.current);
            }
        };
    }, []);

    const onCopy = async() => {
        await navigator.clipboard.writeText(link);
        setCopied(true);

        if (timeoutRef.current !== null) {
            globalThis.window.clearTimeout(timeoutRef.current);
        }
        timeoutRef.current = globalThis.window.setTimeout(() => {
            setCopied(false);
            timeoutRef.current = null;
        }, 1200);
    };

    return (
        <button
            type="button"
            onClick={ () => void onCopy() }
            className="rounded bg-zinc-900 px-2.5 py-1 text-xs font-medium text-white transition hover:bg-zinc-700"
        >
            { copied ? 'COPIED!' : 'COPY' }
        </button>
    );
}
