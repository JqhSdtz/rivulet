import {KeepAlive} from 'react-activation';
import React, {useContext} from 'react';
import {TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {MenuConfigItem} from '@/menuConfig';
import {WrappedComponentFactory} from 'react-sortable-hoc';

export interface TabNodeAttributes {
    isActive?: boolean;
    needAttention?: boolean;
    splitViewIndex?: number;
}

type WrappedComponent<P> =
    | React.ComponentClass<P>
    | React.FC<P>
    | WrappedComponentFactory<P>;

export interface TabComponentType {
    node: WrappedComponent<any>;
    props: any;
}

export type TabNodeType = {
    createTime: number;
    updateTime: number;
    name?: string;
    id: string;
    targetMenu?: MenuConfigItem;
    component?: TabComponentType

    [key: string]: any;
} & TabNodeAttributes;

export interface TabNodeCallbacks {
    beforeCloseCallback?: (clearAttention: () => void, doClose: () => void) => boolean | Promise<boolean>;
}

interface TabNodeCallbacksSetter {
    beforeClose: (callback: (clearAttention: () => void, doClose: () => void) => boolean | Promise<boolean>) => void;
}

export type TabNodeContextType = {
    tabKey: string;
    closeTab: () => void;
} & TabNodeCallbacksSetter;

export const TabNodeContext = React.createContext({} as TabNodeContextType);

export default (props: { tabKey: string; children: any }) => {
    const tabKey = props.tabKey;
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
                _nk=""
            >
                {props.children}
            </KeepAlive>
        </TabNodeContext.Provider>
    );
}
