import { CopyButton } from '@/components/common/CopyButton';

interface SnippetDisplayProps {
    snippetHtml: string;
}

/**
 * Keeps installation instructions and copy behavior together.
 * Rendering snippets through one component prevents formatting and clipboard affordances from drifting
 * between creation and settings flows.
 */
export function SnippetDisplay({ snippetHtml }: SnippetDisplayProps) {
    return (
        <section className="max-w-lg rounded-lg border border-zinc-200 bg-white p-4">
            <h3 className="text-sm font-medium text-zinc-900">Embed Snippet</h3>
            <p className="mt-1 text-xs text-zinc-400">Paste into your website &lt;head&gt; or before &lt;/body&gt;.</p>
            <pre className="mt-3 overflow-x-auto rounded bg-zinc-50 p-3 text-xs text-zinc-600">{ snippetHtml }</pre>
            <div className="mt-2 flex justify-end">
                <CopyButton link={ snippetHtml }/>
            </div>
        </section>
    );
}
