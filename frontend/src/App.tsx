import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { Layout } from '@/components/layout/Layout';
import AddSitePage from '@/pages/AddSitePage';
import DashboardPage from '@/pages/DashboardPage';
import SiteAnalyticsPage from '@/pages/SiteAnalyticsPage';
import SiteSettingsPage from '@/pages/SiteSettingsPage';

function PublicLayout() {
    return (
        <Layout>
            <Outlet/>
        </Layout>
    );
}

export default function App() {
    return (
        <Routes>
            <Route element={ <PublicLayout/> }>
                <Route path="/dashboard" element={ <DashboardPage/> }/>
                <Route path="/sites/new" element={ <AddSitePage/> }/>
                <Route path="/sites/:siteId/analytics" element={ <SiteAnalyticsPage/> }/>
                <Route path="/sites/:siteId/settings" element={ <SiteSettingsPage/> }/>
            </Route>

            <Route path="*" element={ <Navigate to="/dashboard" replace/> }/>
        </Routes>
    );
}
