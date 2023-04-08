import {fixContext, KeepAlive} from 'react-activation';
import React, {ReactElement, RefObject, useContext, useRef} from 'react';
import {SplitViewType, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {MenuConfigItem} from '@/menuConfig';
import {WrappedComponentFactory} from 'react-sortable-hoc';
import {useSortable} from '@dnd-kit/sortable';

export interface TabNodeAttributes {
    title?: string;
    targetMenu?: MenuConfigItem;
    component?: TabComponentType;
    contentRef?: RefObject<HTMLDivElement>;
    tabElement?: ReactElement;
    isActive?: boolean;
    isNewTab?: boolean;
    isRemoving?: boolean;
    isModified?: boolean;
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
    tabNode: TabNodeType;
    closeTab: () => void;
    setTabTitle: (title: string) => void;
} & TabNodeCallbacksSetter;

export const TabNodeContext = React.createContext({} as TabNodeContextType);
fixContext(TabNodeContext);

export default (props: { tabKey: string; contentRef: RefObject<HTMLDivElement>; children: any }) => {
    const tabKey = props.tabKey;
    const {
        removeNode,
        findNode,
        setTabNodeCallbacks,
        setTabNodeAttributes,
        refreshTabNode,
        updateTabs
    } = useContext<TabsContextType>(TabsContext);

    const setTabTitle = (title) => {
        if (tabNode.isRemoving) return;
        setTabNodeAttributes(tabKey, {
            title
        });
        refreshTabNode(tabNode);
        updateTabs();
    };
    const closeTab = () => {
        removeNode(tabKey);
    };
    const beforeClose = callback => {
        setTabNodeCallbacks(tabKey, {
            beforeCloseCallback: callback
        });
    };
    const tabNode = findNode(tabKey);
    const value = {
        tabKey,
        tabNode,
        closeTab,
        setTabTitle,
        beforeClose
    };
    return (
        <TabNodeContext.Provider value={value}>
            <KeepAlive
                id={tabKey}
                name={tabKey}
                cacheKey={tabKey}
                saveScrollPosition="screen"
            >
                <div
                     style={{
                         height: '100%',
                         overflowY: 'scroll'
                     }}
                >
                    {props.children}
                </div>
            </KeepAlive>
        </TabNodeContext.Provider>
    );
}
