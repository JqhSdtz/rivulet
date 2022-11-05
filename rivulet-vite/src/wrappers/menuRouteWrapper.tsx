import {TabsContent, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {RouteConfig} from '@/routes';
import {useContext} from 'react';
import {useLocation, RouteObject} from 'react-router-dom';

type MenuRouteConfig = {
    subMenu?: MenuRouteConfig[]
} & RouteConfig;

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

const processRouteConfig = (routeObjects: RouteObject[], config: MenuRouteConfig) => {
    const routeObject = {...config} as RouteObject;
    routeObject.element = <WrappedComponent {...config}/>;
    if (config.subMenu) {
        config.subMenu.forEach(subMenuConfig => {
            subMenuConfig.path = config.path + '/' + subMenuConfig.path;
            processRouteConfig(routeObjects, subMenuConfig);
        });
    }
    routeObjects.push(routeObject);
};

export default function (routeConfigs: MenuRouteConfig[]): RouteObject[] {
    const routeObjects: RouteObject[] = [];
    routeConfigs.forEach(routeConfig => {
        processRouteConfig(routeObjects, routeConfig);
    });
    return routeObjects;
}
