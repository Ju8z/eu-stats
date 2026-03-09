import React from 'react';
import { Link } from 'react-router-dom';

export class Header extends React.Component {

    render() {
        return (
            <header className="border-b border-zinc-200 bg-white">
                <div className="mx-auto flex h-12 max-w-6xl items-center px-4">
                    <div className="mx-auto text-sm font-semibold tracking-tight text-zinc-900">EU ANALYTICS</div>
                    <Link to="/dashboard" className="text-sm font-semibold tracking-tight text-zinc-900">
                        DASHBOARD
                    </Link>
                </div>
            </header>
        );
    }
}
