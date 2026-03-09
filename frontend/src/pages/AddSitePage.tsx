import React, { useState } from 'react';
import { sitesApi } from '@/api/sitesApi';
import { SnippetDisplay } from '@/components/sites/SnippetDisplay';

export default function AddSitePage() {
    const [name, setName] = useState('');
    const [domain, setDomain] = useState('');
    const [siteId, setSiteId] = useState<number | null>(null);
    const [snippet, setSnippet] = useState<string>('');

    const onSubmit = async(event: React.SyntheticEvent<HTMLFormElement>) => {
        event.preventDefault();
        const created = await sitesApi.create({ name, domain });
        const snippetResponse = await sitesApi.snippet(created.id);
        setSiteId(created.id);
        setSnippet(snippetResponse.snippetHtml);
    };

    return (
        <div className="space-y-4">
            <h1 className="text-lg font-semibold text-zinc-900">ADD SITE</h1>
            <form onSubmit={ onSubmit } className="max-w-lg space-y-3 rounded-lg border border-zinc-200 bg-white p-4">
                <div>
                    <label htmlFor="name" className="mb-1 block text-xs font-medium text-zinc-500">Site Name</label>
                    <input
                        id="name"
                        value={ name }
                        onChange={ (event) => setName(event.target.value) }
                        required
                        className="w-full rounded border border-zinc-300 px-3 py-1.5 text-sm outline-none focus:border-blue-500"
                        placeholder="My Blog"
                    />
                </div>
                <div>
                    <label htmlFor="domain" className="mb-1 block text-xs font-medium text-zinc-500">Domain</label>
                    <input
                        id="domain"
                        value={ domain }
                        onChange={ (event) => setDomain(event.target.value) }
                        required
                        className="w-full rounded border border-zinc-300 px-3 py-1.5 text-sm outline-none focus:border-blue-500"
                        placeholder="example.com"
                    />
                </div>
                <button type="submit" disabled={ siteId !== null }
                        className="rounded bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60">
                    CREATE SITE
                </button>
            </form>

            { siteId && snippet ? (
                <div className="space-y-2">
                    <p className="text-sm text-zinc-500">Site #{ siteId } created. Add this snippet to your website.</p>
                    <SnippetDisplay snippetHtml={ snippet }/>
                </div>
            ) : null }
        </div>
    );
}
