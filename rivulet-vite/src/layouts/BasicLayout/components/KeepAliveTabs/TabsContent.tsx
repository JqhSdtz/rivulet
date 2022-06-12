import {useContext, useRef} from 'react';
import {SplitViewType, TabNodeProvider, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {useEventListener} from 'ahooks';

const SplitView = (props: { splitView: SplitViewType }) => {
    const {
        splitViewContainer,
        updateTabs
    } = useContext<TabsContextType>(TabsContext);
    const {splitView} = props;
    const activeTab = splitView.tabNodes.find(node => node.isActive);
    const tabComponent = activeTab?.component;
    if (!tabComponent) {
        return <></>;
    }
    const Component = tabComponent.node;
    const width = 100 / splitViewContainer.splitViews.length + '%';
    const ref = useRef();
    useEventListener('click', () => {
        splitViewContainer.splitViews.forEach(tmpSplitView => tmpSplitView.isActive = false);
        splitView.isActive = true;
        updateTabs();
    }, {target: ref});
    return (
        <div key={activeTab.name}
             style={{width, display: 'inline-block'}}
             ref={ref}>
            <TabNodeProvider tabKey={activeTab.name ?? ''}>
                <Component {...tabComponent.props}/>
            </TabNodeProvider>
        </div>
    );
};

export default () => {
    const {
        splitViewContainer
    } = useContext<TabsContextType>(TabsContext);
    return (
        <div className="keep-alive-tab-content" style={{display: 'flex'}}>
            {splitViewContainer.splitViews.map(splitView => <SplitView splitView={splitView}/>)}
        </div>
    );
}
