import {CachingNodeType} from './CachingNode';
import {useHistory, useLocation} from 'ice';
import {useAliveController} from 'react-activation';
import React, {useContext, useState} from 'react';
import {RouteContext} from '@/layouts/BasicLayout';
import {MenuConfigItem} from '@/layouts/BasicLayout/configs/menuConfig';

function sortCachingNodes(tabKeySequence, cachingNodes): CachingNodeType[] {
    const sortedCachingNodes: CachingNodeType[] = [];
    tabKeySequence.forEach(tabKey => {
        const cachingNode = cachingNodes.find(node => node.name === tabKey);
        if (cachingNode) {
            sortedCachingNodes.push(cachingNode);
        }
    });
    return sortedCachingNodes;
}

function fillCachingNodeWithMenuData(sortedCachingNodes, menuData) {
    sortedCachingNodes.forEach(node => {
        menuData?.forEach((menu: MenuConfigItem) => {
            if (menu.testPath(node?.name)) {
                node.targetMenu = menu;
            }
        });
    });
}

function synchronizeTabKeySequence(prevTabKeySequence, cachingNodes): string[] {
    let curTabKeySequence = prevTabKeySequence.filter(tabKey => cachingNodes.findIndex(node => node.name === tabKey) !== -1);
    if (cachingNodes.length > prevTabKeySequence.length) {
        const addedTabKey: [] = cachingNodes.slice(prevTabKeySequence.length).map(node => node.name);
        curTabKeySequence = curTabKeySequence.concat(addedTabKey);
    }
    return curTabKeySequence;
}

export interface TabsContextType {
    sortedCachingNodes: CachingNodeType[]
    tabKeySequence: string[]
    setTabKeySequence: (tabKeySequence: string[]) => void
    currentPath: string
    activeNode: (targetKey: string | undefined) => void
    removeNode: (targetKey: string | undefined) => void
    refreshNode: (targetKey: string | undefined) => void
    removeOtherNodes: (targetKey) => void
    removeLeftSideNodes: (targetKey) => void
    removeRightSideNodes: (targetKey) => void
}

export const TabsContext = React.createContext({} as TabsContextType);

export default (props) => {
    const {getCachingNodes, drop, refresh} = useAliveController();
    const cachingNodes = getCachingNodes();
    const {menuData} = useContext(RouteContext);
    const {pathname, search} = useLocation();
    const history = useHistory();
    const currentPath = pathname + search;
    let [tabKeySequence, setTabKeySequence] = useState([] as string[]);
    // 将cachingNodes中的增加和删除反映到tabKeySequence中
    tabKeySequence = synchronizeTabKeySequence(tabKeySequence, cachingNodes);
    // 对cachingNodes进行排序
    const sortedCachingNodes = sortCachingNodes(tabKeySequence, cachingNodes);
    // 设置cachingNode对应的MenuItem
    fillCachingNodeWithMenuData(sortedCachingNodes, menuData);
    const activeNode = (targetKey) => {
        if (!targetKey) return;
        history.push(targetKey || '');
    }
    const removeNode = (targetKey) => {
        if (!targetKey) return;
        const isActive = targetKey === currentPath;
        if (isActive) {
            drop(targetKey).then(() => {
            });
            const curIdx = sortedCachingNodes.findIndex(
                routeNode => routeNode.name === targetKey,
            );
            activeNode(curIdx > 0 ? sortedCachingNodes[curIdx - 1].name || '' : '');
        } else {
            drop(targetKey).then(() => {
            });
        }
    }
    const refreshNode = (targetKey) => {
        if (!targetKey) return;
        refresh(targetKey).then(() => {
        });
    };
    const removeOtherNodes = (targetKey) => {
        activeNode(targetKey);
        sortedCachingNodes.forEach(node => {
            if (node.name !== targetKey) {
                drop(node.name || '').then(() => {
                });
            }
        });
    };
    const removeLeftSideNodes = (targetKey) => {
        for (let i = 0; i < sortedCachingNodes.length; ++i) {
            const node = sortedCachingNodes[i];
            if (node.name === targetKey) {
                return;
            }
            if (node.name === currentPath) {
                activeNode(targetKey);
            }
            drop(node.name || '').then(() => {
            });
        }
    }
    const removeRightSideNodes = (targetKey) => {
        for (let i = sortedCachingNodes.length - 1; i >= 0; --i) {
            const node = sortedCachingNodes[i];
            if (node.name === targetKey) {
                return;
            }
            if (node.name === currentPath) {
                activeNode(targetKey);
            }
            drop(node.name || '').then(() => {
            });
        }
    }

    const value: TabsContextType = {
        sortedCachingNodes,
        tabKeySequence,
        setTabKeySequence,
        currentPath,
        activeNode,
        removeNode,
        refreshNode,
        removeOtherNodes,
        removeLeftSideNodes,
        removeRightSideNodes,
    };
    return (
        <TabsContext.Provider value={value}>
            {props.children}
        </TabsContext.Provider>
    )
}
