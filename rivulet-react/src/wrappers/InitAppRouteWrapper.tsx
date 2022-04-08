import {Redirect, useAuth} from 'ice';
import store from '@/store';

export default (WrappedComponent) => {
    return (props) => {
        const appState = store.useModelState('app');
        if (appState.appInitialized) {
            const [auth] = useAuth();
            if (auth.hasLoggedIn) {
                return <Redirect to="/"/>;
            } else {
                return <Redirect to="/login"/>;
            }
        }
        return <WrappedComponent {...props} />;
    };
};
