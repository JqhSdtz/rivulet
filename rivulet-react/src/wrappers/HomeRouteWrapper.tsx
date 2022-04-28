import {Redirect, useAuth} from 'ice';
import store from '@/store';

let beforeLogValidPath = '';

export default (WrappedComponent) => {
    return (props) => {
        const appState = store.useModelState('app');
        if (!appState.appInitialized) {
            return <Redirect to="/initApp"/>;
        }
        const [auth] = useAuth();
        if (!auth.hasLoggedIn) {
            const {pathname, search} = props.location;
            beforeLogValidPath = pathname + search;
            return <Redirect to="/login"/>;
        } else {
            if (beforeLogValidPath) {
                const redirect = <Redirect to={beforeLogValidPath}/>;
                beforeLogValidPath = '';
                return redirect;
            } else {
                return <WrappedComponent {...props} />;
            }
        }
    };
};
