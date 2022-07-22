import BasicLayout from '@/layouts/BasicLayout';
import Home from '@/pages/Home';
import Dashboard from '@/pages/Dashboard';
import DataModel from '@/pages/DataModel';
import React from 'react';
import homeRouteWrapper from '@/wrappers/homeRouteWrapper';
import menuRouteWrapper from '@/wrappers/menuRouteWrapper';
import {RouteObject} from 'react-router/lib/router';
import Login from '@/pages/Login';
import loginRouteWrapper from '@/wrappers/loginRouteWrapper';
import InitApp from '@/pages/InitApp';
import initAppRouteWrapper from '@/wrappers/initAppRouteWrapper';
import ModelDetail from '@/pages/DataModel/ModelDetail';

export interface RouteConfig {
    children?: RouteConfig[];
    path?: string;
    component?: React.ComponentType<any>;
}

const routesConfig: RouteObject[] = [
    loginRouteWrapper({
        path: '/login',
        component: Login
    }),
    initAppRouteWrapper({
        path: '/initApp',
        component: InitApp
    }),
    homeRouteWrapper({
        path: '/',
        component: BasicLayout,
        children: menuRouteWrapper([
            {
                path: 'dashboard',
                component: Dashboard
            },
            {
                path: 'data_model',
                component: DataModel,
                subMenu: [
                    {
                        path: 'detail',
                        component: ModelDetail
                    }
                ]
            },
            {
                path: 'test',
                component: DataModel
            },
            {
                path: '',
                component: Home
            }
        ])
    })
];

export default routesConfig;
