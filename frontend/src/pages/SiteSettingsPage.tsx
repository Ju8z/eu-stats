import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { sitesApi } from '../api/sitesApi';
import { SnippetDisplay } from '../components/sites/SnippetDisplay';

export default function SiteSettingsPage() {
    const { siteId } = useParams();
    const numericSiteId = Number(siteId);
    const navigate = useNavigate();

    const [name, setName] = useState('');
    const [domain, setDomain] = useState('');
    const [snippet, setSnippet] = useState('');
    const [saved, setSaved] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

    useEffect(() => {
        sitesApi.get(numericSiteId).then((site) => {
            setName(site.name);
            setDomain(site.domain);
        });
        sitesApi.snippet(numericSiteId).then((response) => setSnippet(response.snippetHtml));
    }, [numericSiteId]);

    const onSubmit = async(event: React.SyntheticEvent<HTMLFormElement>) => {
        event.preventDefault();
        await sitesApi.update(numericSiteId, { name, domain });
        setSaved(true);
        globalThis.setTimeout(() => setSaved(false), 2000);
    };

    const onDelete = async() => {
        if (!globalThis.window.confirm('Delete this site and all related analytics data permanently?')) {
            return;
        }

        setIsDeleting(true);
        try {
            await sitesApi.remove(numericSiteId);
            navigate('/dashboard');
        } finally {
            setIsDeleting(false);
        }
    };

    return (
        <div className="space-y-4">
            <h1 className="text-lg font-semibold text-zinc-900">SITE SETTINGS</h1>
            <form onSubmit={ onSubmit } className="max-w-lg space-y-3 rounded-lg border border-zinc-200 bg-white p-4">
                <input value={ name } onChange={ (event) => setName(event.target.value) }
                       className="w-full rounded border border-zinc-300 px-3 py-1.5 text-sm outline-none focus:border-blue-500"/>
                <input value={ domain } onChange={ (event) => setDomain(event.target.value) }
                       className="w-full rounded border border-zinc-300 px-3 py-1.5 text-sm outline-none focus:border-blue-500"/>
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <button type="submit" className="rounded bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700">
                            SAVE
                        </button>
                        { saved && <span className="text-xs font-medium text-green-600">SAVED!</span> }
                    </div>
                    <button
                        type="button"
                        onClick={ () => void onDelete() }
                        disabled={ isDeleting }
                        className="rounded bg-red-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-60"
                    >
                        { isDeleting ? 'Deleting...' : 'DELETE SITE' }
                    </button>
                </div>
            </form>
            { snippet && <SnippetDisplay snippetHtml={ snippet }/> }
        </div>
    );
}
