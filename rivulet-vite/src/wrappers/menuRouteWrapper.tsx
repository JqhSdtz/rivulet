import {TabsContent, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {RouteConfig} from '@/routes';
import {RouteObject} from 'react-router/lib/router';
import {useContext} from 'react';
import {useLocation} from 'react-router-dom';

const WrappedComponent = (config: RouteConfig) => {
    const {pathname, search} = useLocation();
    const path = pathname + search;
    const {
        findNode,
        refreshTabNode,
        setTabNodeAttributes
    } = useContext<TabsContextType>(TabsContext);
    setTabNodeAttributes(path, {
        component: {
            node: config.component,
            props: {}
        }
    });
    const tabNode = findNode(path);
    if (tabNode) {
        refreshTabNode(tabNode);
    }
    return <TabsContent/>;
};

export default function (routeConfigs: RouteConfig[]): RouteObject[] {
    const routeObjects = routeConfigs.map(routeConfig => {
        const routeObject = {...routeConfig} as RouteObject;
        routeObject.element = <WrappedComponent {...routeConfig}/>;
        return routeObject;
    });
    return routeObjects;
}
