import {IRouterConfig} from 'ice';
import Layout from '@/layouts/BasicLayout';
import NotFound from '@/components/NotFound';
import Home from '@/pages/Home';
import Login from '@/pages/Login';
import Dashboard from '@/pages/Dashboard';
import DataModel from '@/pages/DataModel';
import InitApp from '@/pages/InitApp';
import HomeRouteWrapper from '@/wrappers/HomeRouteWrapper';
import LoginRouteWrapper from '@/wrappers/LoginRouteWrapper';
import InitAppRouteWrapper from '@/wrappers/InitAppRouteWrapper';
import ContentRouteWrapper from '@/wrappers/MenuRouteWrapper';

const menuRouterConfig: IRouterConfig[] = [
    {
        path: '/dashboard',
        component: Dashboard
    },
    {
        path: '/data_model',
        component: DataModel
    },
    {
        path: '/test',
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
];

menuRouterConfig.forEach(menuRouter => {
   menuRouter.wrappers = [ContentRouteWrapper];
});

const routerConfig: IRouterConfig[] = [
    {
        path: '/login',
        component: Login,
        wrappers: [LoginRouteWrapper]
    },
    {
        path: '/initApp',
        component: InitApp,
        wrappers: [InitAppRouteWrapper]
    },
    {
        path: '/',
        component: Layout,
        wrappers: [HomeRouteWrapper],
        children: menuRouterConfig
    }
];

export default routerConfig;
