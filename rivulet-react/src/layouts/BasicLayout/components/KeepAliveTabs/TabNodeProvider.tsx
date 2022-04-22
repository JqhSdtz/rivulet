import {useLocation} from 'ice';
import {KeepAlive} from 'react-activation';
import React, {useContext} from 'react';
import {TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {MenuConfigItem} from '@/menuConfig';

export interface TabStatus {
    needAttention?: boolean
}

export type TabNodeType = {
    createTime: number;
    updateTime: number;
    name?: string;
    id: string;
    targetMenu?: MenuConfigItem;

    [key: string]: any;
} & TabStatus;

export interface TabNodeCallbacks {
    beforeCloseCallback?: (clearAttention: () => void, doClose: () => void) => boolean | Promise<boolean>
}

interface TabNodeCallbacksSetter {
    beforeClose: (callback: (clearAttention: () => void, doClose: () => void) => boolean| Promise<boolean>) => void
}

export type TabNodeContextType = {
    tabKey: string;
    closeTab: () => void;
} & TabNodeCallbacksSetter;

export const TabNodeContext = React.createContext({} as TabNodeContextType);

export default (props) => {
    const {pathname, search} = useLocation();
    const tabKey = pathname + search;
    const {
        removeNode,
        setTabNodeCallbacks
    } = useContext<TabsContextType>(TabsContext);
    const closeTab = () => {
        removeNode(tabKey);
    };
    const beforeClose = callback => {
        setTabNodeCallbacks(tabKey, {
            beforeCloseCallback: callback
        });
    };
    const value = {
        tabKey,
        closeTab,
        beforeClose
    };
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
