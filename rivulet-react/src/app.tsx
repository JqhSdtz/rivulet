import {IAppConfig, request, runApp} from 'ice';
import remHelper from '@/utils/remHelper';
import {PageLoading} from '@/layouts/BasicLayout';

const appConfig: IAppConfig = {
    request: {
        baseURL: '/api'
    },
    app: {
        // strict: true,
        rootId: 'ice-container',
        getInitialData: async (ctx) => {
            const appInitialData = await request.get('/app/initialData');
            const currentUser = appInitialData.currentUser;
            const initialStates = {
                app: appInitialData.appState,
                user: appInitialData.currentUser
            };
            const auth = {
                hasLoggedIn: !!currentUser
            };
            return {
                initialStates: initialStates,
                auth: auth
            };
        }
    },
    router: {
        type: 'browser',
        fallback: <PageLoading/>
    }
};

remHelper.initRem({
    pcWidth: 1280,
    maxFontSize: 18,
    pcOnly: true,
    responsive: true
});

runApp(appConfig);
