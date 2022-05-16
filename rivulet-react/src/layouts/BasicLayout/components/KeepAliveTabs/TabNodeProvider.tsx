import {KeepAlive} from 'react-activation';
import React, {ReactElement, useContext} from 'react';
import {SplitViewType, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {MenuConfigItem} from '@/menuConfig';
import {WrappedComponentFactory} from 'react-sortable-hoc';
import {useSortable} from '@dnd-kit/sortable';

export interface TabNodeAttributes {
    targetMenu?: MenuConfigItem;
    component?: TabComponentType;
    tabElement?: ReactElement;
    isActive?: boolean;
    isNewTab?: boolean;
    needAttention?: boolean;
    sortableAttr?: ReturnType<typeof useSortable>;
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
    splitView: SplitViewType;

    [key: string]: any;
} & TabNodeAttributes & TabNodeCallbacks;

export interface TabNodeCallbacks {
    beforeCloseCallback?(clearAttention: () => void, doClose: () => void): boolean | Promise<boolean>;
}

interface TabNodeCallbacksSetter {
    beforeClose(callback: (clearAttention: () => void, doClose: () => void) => boolean | Promise<boolean>): void;
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
            >
                {props.children}
            </KeepAlive>
        </TabNodeContext.Provider>
    );
}
