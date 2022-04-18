const state = {
    appInitialized: false,
}
type IAppState = typeof state;

export default {
    state: state,
    reducers: {
        finishAppInit(prevState: IAppState) {
            prevState.appInitialized = true;
        },
    },
}
