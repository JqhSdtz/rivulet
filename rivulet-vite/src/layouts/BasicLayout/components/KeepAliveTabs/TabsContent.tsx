import {useContext, useRef} from 'react';
import {SplitViewType, TabNodeProvider, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {useEventListener, useSize} from 'ahooks';
import {ErrorBoundary} from '@ant-design/pro-utils';

const SplitView = (props: { splitView: SplitViewType }) => {
    const {
        splitViewContainer,
        activeNode,
        updateTabs
    } = useContext<TabsContextType>(TabsContext);
    const {splitView} = props;
    const activeTab = splitView.tabNodes.find(node => node.isActive);
    const tabComponent = activeTab?.component;
    const Component = tabComponent.node;
    const width = 100 / splitViewContainer.splitViews.length + '%';
    const ref = useRef<HTMLDivElement>();
    useEventListener('click', () => {
        if (splitView.isActive) return;
        splitViewContainer.splitViews.forEach(tmpSplitView => tmpSplitView.isActive = false);
        splitView.isActive = true;
        activeNode(activeTab.name);
        updateTabs();
    }, {target: ref});
    const tabsSize = useSize(document.getElementsByClassName('keep-alive-tabs')[0]);
    splitView.contentRef = ref;
    return (
        <div key={activeTab.name}
             style={{
                 width,
                 height: `calc(100vh - ${tabsSize?.height ?? 0}px)`,
                 display: 'inline-block',
                 position: 'relative',
                 transform: 'scale(1,1)'
             }}
             ref={ref}>
            <TabNodeProvider tabKey={activeTab.name ?? ''} contentRef={ref}>
                <ErrorBoundary>
                    <Component {...tabComponent.props}/>
                </ErrorBoundary>
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
            {splitViewContainer.splitViews.map(splitView => {
                const activeTab = splitView.tabNodes.find(node => node.isActive);
                if (!activeTab?.component) {
                    return null;
                }
                return <SplitView key={splitView.id} splitView={splitView}/>;
            })}
        </div>
    );
}
