import store from '@/store';
import {Navigate, RouteObject, useLocation} from 'react-router-dom';
import {RouteConfig} from '@/routes';

let beforeLogValidPath = '';

const WrappedComponent = (config: RouteConfig) => {
    const location = useLocation();
    const appState = store.useModelState('app');
    if (!appState.appInitialized) {
        return <Navigate to="/initApp"/>;
    }
    const authState = store.useModelState('auth');
    if (!authState.hasLoggedIn) {
        const {pathname, search} = location;
        beforeLogValidPath = pathname + search;
        return <Navigate to="/login"/>;
    } else {
        if (beforeLogValidPath) {
            const redirect = <Navigate to={beforeLogValidPath}/>;
            beforeLogValidPath = '';
            return redirect;
        } else {
            const Component = config.component;
            return <Component/>;
        }
    }
};

export default function (routeConfig: RouteConfig): RouteObject {
    const routeObject = {...routeConfig} as RouteObject;
    routeObject.element = <WrappedComponent {...routeConfig}/>;
    return routeObject;
}
