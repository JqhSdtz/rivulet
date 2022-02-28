import {IRouterConfig} from 'ice';
import Layout from '@/layouts/BasicLayout';
import NotFound from '@/components/NotFound';
import Home from '@/pages/Home';
import Dashboard from '@/pages/Dashboard';
import DataModel from '@/pages/DataModel';

const routerConfig: IRouterConfig[] = [
    {
        path: '/',
        component: Layout,
        children: [
            {
                path: '/dashboard',
                component: Dashboard
            },
            {
                path: '/data_model',
                component: DataModel
            },
            {
                path: '/',
                exact: true,
                component: Home
            },
            {
                component: NotFound
            }
        ]
    }
];

export default routerConfig;
