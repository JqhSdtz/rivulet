import {IAppConfig, runApp} from 'ice';
import remHelper from '@/utils/rem-helper';
import {PageLoading} from '@/layouts/BasicLayout';

const appConfig: IAppConfig = {
    app: {
        rootId: 'ice-container'
    },
    router: {
        type: 'browser',
        fallback: <PageLoading />
    }
};

remHelper.initRem({
    pcWidth: 1280,
    maxFontSize: 18,
    pcOnly: true,
    responsive: true
});

runApp(appConfig);
