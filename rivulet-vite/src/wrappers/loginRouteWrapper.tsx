import store from '@/store';
import {RouteConfig} from '@/routes';
import {Navigate, RouteObject} from 'react-router-dom';

const WrappedComponent = (config: RouteConfig) => {
    const appState = store.useModelState('app');
    if (!appState.appInitialized) {
        return <Navigate to="/initApp"/>;
    }
    const authState = store.useModelState('auth');
    if (authState.hasLoggedIn) {
        return <Navigate to="/"/>;
    }
    const Component = config.component;
    return <Component/>;
};

export default function (routeConfig: RouteConfig): RouteObject {
    const routeObject = {...routeConfig} as RouteObject;
    routeObject.element = <WrappedComponent {...routeConfig}/>;
    return routeObject;
};
