import {Redirect, useAuth} from 'ice';
import store from '@/store';

export default (WrappedComponent) => {
    return (props) => {
        const appState = store.useModelState('app');
        if (!appState.appInitialized) {
            return <Redirect to="/initApp"/>;
        }
        const [auth] = useAuth();
        if (auth.hasLoggedIn) {
            return <Redirect to="/"/>;
        }
        return <WrappedComponent {...props} />;
    };
};
