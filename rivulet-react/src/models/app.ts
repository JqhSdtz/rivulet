const state = {
    appInitialized: false,
    beforeLogValidPath: ''
};
type IAppState = typeof state;

export default {
    state: state,
    reducers: {
        finishAppInit(prevState: IAppState) {
            prevState.appInitialized = true;
        }
    }
};
