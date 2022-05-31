import store from '@/store';
import {RouteConfig} from '@/routes';
import {RouteObject} from 'react-router/lib/router';
import {Navigate} from 'react-router-dom';

const WrappedComponent = (config: RouteConfig) => {
    const appState = store.useModelState('app');
    if (appState.appInitialized) {
        const authState = store.useModelState('auth');
        if (authState.hasLoggedIn) {
            return <Navigate to="/"/>;
        } else {
            return <Navigate to="/login"/>;
        }
    }
    const Component = config.component;
    return <Component/>;
}

export default function (routeConfig: RouteConfig): RouteObject {
    const routeObject = {...routeConfig} as RouteObject;
    routeObject.element = <WrappedComponent {...routeConfig}/>;
    return routeObject;
};
