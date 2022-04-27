import {useContext} from 'react';
import {TabNodeProvider, TabsContext, TabsContextType} from '@/layouts/BasicLayout';

export default () => {
    const {
        splitViews,
        sortedTabNodes
    } = useContext<TabsContextType>(TabsContext);
    const elements = sortedTabNodes.map(node => {
        const tabComponent = node.component;
        if (!tabComponent) {
            return <div key={node.name}></div>;
        }
        const Component = tabComponent.node;
        const width = 100 / splitViews.length + '%';
        return (
            <div key={node.name} style={{width, display: 'inline-block'}}>
                <TabNodeProvider tabKey={node.name ?? ''}>
                    <Component {...tabComponent.props}/>
                </TabNodeProvider>
            </div>
        );
    });
    return (
        <div className="keep-alive-tab-content">
            {elements}
        </div>
    );
}
