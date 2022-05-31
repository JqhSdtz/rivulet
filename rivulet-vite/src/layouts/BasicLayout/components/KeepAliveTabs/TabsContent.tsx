import {ReactElement, useContext} from 'react';
import {TabNodeProvider, TabsContext, TabsContextType} from '@/layouts/BasicLayout';

export default () => {
    const {
        splitViewContainer
    } = useContext<TabsContextType>(TabsContext);
    const splitViewElements = [] as ReactElement[];
    splitViewContainer.splitViews.forEach(splitView => {
        const activeTab = splitView.tabNodes.find(node => node.isActive);
        const tabComponent = activeTab?.component;
        if (!tabComponent) {
            return;
        }
        const Component = tabComponent.node;
        const width = 100 / splitViewContainer.splitViews.length + '%';
        splitViewElements.push(
            <div key={activeTab.name} style={{width, display: 'inline-block'}}>
                <TabNodeProvider tabKey={activeTab.name ?? ''}>
                    <Component {...tabComponent.props}/>
                </TabNodeProvider>
            </div>
        );
    });
    return (
        <div className="keep-alive-tab-content">
            {splitViewElements}
        </div>
    );
}
