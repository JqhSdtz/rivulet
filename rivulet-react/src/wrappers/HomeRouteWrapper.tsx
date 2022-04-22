import {Redirect, useAuth, useLocation} from 'ice';
import store from '@/store';

export default (WrappedComponent) => {
    return (props) => {
        const [appState, appDispatchers] = store.useModel('app');
        if (!appState.appInitialized) {
            return <Redirect to="/initApp"/>;
        }
        const [auth] = useAuth();
        if (!auth.hasLoggedIn) {
            const {pathname, search} = useLocation();
            appDispatchers.setState({beforeLogValidPath: pathname + search});
            return <Redirect to="/login"/>;
        } else {
            if (appState.beforeLogValidPath) {
                appDispatchers.setState({beforeLogValidPath: ''});
                return <Redirect to={appState.beforeLogValidPath}/>;
            } else {
                return <WrappedComponent {...props} />;
            }
        }
    };
};
