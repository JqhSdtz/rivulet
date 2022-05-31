import {BrowserRouter, useRoutes} from 'react-router-dom';
import routesConfig from '@/routes';
import {useState} from 'react';
import {useAsyncEffect, useMount} from 'ahooks';
import {PageLoading} from '@/layouts/BasicLayout';
import axios from 'axios';
import store from '@/store';

let storeInitialized = false;

const AppRoutes = () => useRoutes(routesConfig);

const getInitialData = async () => {
    const response = await axios.get('/app/initialData');
    const appInitialData = response.data;
    const currentUser = appInitialData.currentUser;
    const initialStates = {
        app: appInitialData.appState,
        user: appInitialData.currentUser,
        auth: {
            hasLoggedIn: !!currentUser
        }
    };
    return initialStates;
};

export default () => {
    const [root, setRoot] = useState(() => (
        <PageLoading/>
    ));
    // react18在开发模式中会故意调用两次useEffect来保证其中不包含副作用，生产模式不会故意调用两次
    useAsyncEffect(async () => {
        if (storeInitialized) return;
        storeInitialized = true;
        const initialStates = await getInitialData();
        const IceStoreProvider = store.Provider;
        setRoot(
            <IceStoreProvider initialStates={initialStates}>
                <BrowserRouter>
                    <AppRoutes/>
                </BrowserRouter>
            </IceStoreProvider>
        );
    }, []);
    return root;
}
