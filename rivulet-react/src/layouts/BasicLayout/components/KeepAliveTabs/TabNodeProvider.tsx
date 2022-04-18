import {useLocation} from 'ice';
import {KeepAlive} from 'react-activation';
import React, {useContext} from 'react';
import {TabsContext, TabsContextType} from '@/layouts/BasicLayout';

export interface CachingNodeType {
    createTime: number
    updateTime: number
    name?: string
    id: string

    [key: string]: any
}

export interface TabNodeContextType {
    tabKey: string
    closeTab: () => void
}

export const TabNodeContext = React.createContext({} as TabNodeContextType);

export default (props) => {
    const {pathname, search} = useLocation();
    const tabKey = pathname + search;
    const {
        removeNode,
    } = useContext<TabsContextType>(TabsContext);
    const closeTab = () => {
        removeNode(tabKey);
    }
    const value = {
        tabKey,
        closeTab,
    }
    return (
        <TabNodeContext.Provider value={value}>
            <KeepAlive
                name={tabKey}
                id={tabKey}
                saveScrollPosition="screen"
            >
                {props.children}
            </KeepAlive>
        </TabNodeContext.Provider>
    );
}
