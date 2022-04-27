import {TabsContent, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {useContext} from 'react';

export default (WrappedComponent) => {
    return (props) => {
        const {pathname, search} = props.location;
        const path = pathname + search;
        const {
            findNode
        } = useContext<TabsContextType>(TabsContext);
        const tabNode = findNode(path);
        if (tabNode) {
            tabNode.component = {
                node: WrappedComponent,
                props: props
            };
        }
        return <TabsContent {...props}/>;
    };
}
