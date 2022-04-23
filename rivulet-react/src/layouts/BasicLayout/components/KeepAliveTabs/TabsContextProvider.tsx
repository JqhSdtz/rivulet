import {TabNodeAttributes, TabNodeCallbacks, TabNodeType} from './TabNodeProvider';
import {useHistory, useLocation} from 'ice';
import {useAliveController} from 'react-activation';
import React, {useContext, useState} from 'react';
import {RouteContext} from '@/layouts/BasicLayout';
import {defaultStartPage, MenuConfigItem} from '@/menuConfig';
import {useCreation, useLatest, useMap} from 'ahooks';

function sortCachingNodes(tabKeySequence, cachingNodes): TabNodeType[] {
    const sortedTabNodes: TabNodeType[] = [];
    tabKeySequence.forEach(tabKey => {
        const cachingNode = cachingNodes.find(node => node.name === tabKey);
        if (cachingNode) {
            sortedTabNodes.push(cachingNode);
        }
    });
    return sortedTabNodes;
}

function matchMenuConfig(menuItems: MenuConfigItem[], node: TabNodeType) {
    if (node.targetMenu) {
        return;
    }
    for (let i = 0; i < menuItems.length; ++i) {
        const menu = menuItems[i];
        if (menu.testPath?.(node?.name) ?? false) {
            node.targetMenu = menu;
            return;
        }
        if (menu.routes) {
            matchMenuConfig(menu.routes as MenuConfigItem[], node);
        }
    }
}

function fillCachingNodeWithMenuDataAndStatus(
    sortedTabNodes: TabNodeType[],
    menuData: MenuConfigItem[],
    tabAttributesMap: Map<string, TabNodeAttributes>,
    currentTabKey: string
) {
    sortedTabNodes.forEach(node => {
        if (!node.targetMenu) {
            matchMenuConfig(menuData, node);
        }
        node.isActive = node.name === currentTabKey;
        const tabAttributes = tabAttributesMap.get(node.name ?? '');
        if (tabAttributes) {
            for (const [key, value] of Object.entries(tabAttributes)) {
                node[key] = value;
            }
        }
    });
}

function synchronizeTabKeySequence(prevTabKeySequence, cachingNodes): string[] {
    let curTabKeySequence = prevTabKeySequence.filter(tabKey => cachingNodes.findIndex(node => node.name === tabKey) !== -1);
    if (cachingNodes.length > curTabKeySequence.length) {
        const addedTabKey: [] = cachingNodes.slice(curTabKeySequence.length).map(node => node.name);
        curTabKeySequence = curTabKeySequence.concat(addedTabKey);
    }
    return curTabKeySequence;
}

interface TabNodeOperations {
    findNode: (targetKey: string | undefined) => TabNodeType | undefined;
    activeNode: (targetKey: string | undefined) => void;
    removeNode: (targetKey: string | undefined) => void;
    refreshNode: (targetKey: string | undefined) => void;
    removeAllNodes: () => void;
    removeOtherNodes: (targetKey) => void;
    removeLeftSideNodes: (targetKey) => void;
    removeRightSideNodes: (targetKey) => void;
}

export type TabsContextType = {
    sortedTabNodes: TabNodeType[];
    tabKeySequence: string[];
    currentTabKey: string;
    currentTabNode: TabNodeType | undefined;
    setTabKeySequence: (tabKeySequence: string[]) => void;
    setTabNodeAttributes: (targetKey: string | undefined, attributes: TabNodeAttributes) => void;
    setTabNodeCallbacks: (targetKey: string | undefined, callbacks: TabNodeCallbacks) => void;
} & TabNodeOperations;

export const TabsContext = React.createContext({} as TabsContextType);

export default (props) => {
    const {getCachingNodes, drop, refresh} = useAliveController();
    const cachingNodes = getCachingNodes();
    const {menuData} = useContext(RouteContext);
    const {pathname, search} = useLocation();
    const history = useHistory();
    const currentTabKey = pathname + search;
    const [tabAttributesMap, tabAttributesHandler] = useMap<string, TabNodeAttributes>([]);
    let [tabKeySequence, setTabKeySequence] = useState([] as string[]);
    const tabNodeCallbacksMap = useCreation(() => new Map<string, TabNodeCallbacks>(), []);
    // 将cachingNodes中的增加和删除反映到tabKeySequence中
    tabKeySequence = synchronizeTabKeySequence(tabKeySequence, cachingNodes);
    // 对cachingNodes进行排序，因为涉及回调，可能导致闭包问题，所以使用useLatest包裹
    const sortedTabNodesRef = useLatest(sortCachingNodes(tabKeySequence, cachingNodes));
    // 设置cachingNode对应的MenuItem
    fillCachingNodeWithMenuDataAndStatus(sortedTabNodesRef.current, menuData as MenuConfigItem[], tabAttributesMap, currentTabKey);
    const findNode = targetKey => sortedTabNodesRef.current.find(node => node.name === targetKey ?? '') as TabNodeType | undefined;
    const currentTabNode = findNode(currentTabKey);
    const setTabNodeAttributes = (targetKey, value) => {
        const prevAttr = tabAttributesHandler.get(targetKey);
        tabAttributesHandler.set(targetKey, {
            ...prevAttr,
            ...value
        });
    };
    const activeNode = (targetKey) => {
        if (!targetKey) return;
        history.push(targetKey ?? '');
    };
    const activePrevNode = (targetKey) => {
        const curIdx = sortedTabNodesRef.current.findIndex(
            node => node.name === targetKey
        );
        if (curIdx === 0) {
            activeNode(sortedTabNodesRef.current[1].name ?? '');
        } else if (curIdx > 0) {
            activeNode(sortedTabNodesRef.current[curIdx - 1].name ?? '');
        }
    };
    const activeStartPage = () => activeNode(defaultStartPage);
    const dropNode = async (targetKey, dropSync = false, fromCallback = false) => {
        const doClose = () => {
            removeNode(targetKey, true);
        };
        const doDrop = async () => {
            if (dropSync) {
                await drop(targetKey);
            } else {
                drop(targetKey).then(() => {
                });
            }
        };
        if (fromCallback) {
            await doDrop();
            return true;
        }
        const callbacks = tabNodeCallbacksMap.get(targetKey);
        const prevAttributes = tabAttributesHandler.get(targetKey);
        const clearAttention = () => {
            tabAttributesHandler.set(targetKey, {
                ...prevAttributes,
                needAttention: false
            });
        };
        if (callbacks?.beforeCloseCallback) {
            const shouldClose = await callbacks.beforeCloseCallback(clearAttention, doClose);
            if (shouldClose) {
                await doDrop();
            } else {
                tabAttributesHandler.set(targetKey, {
                    ...prevAttributes,
                    needAttention: true
                });
                return false;
            }
        } else {
            await doDrop();
        }
        return true;
    };
    const removeNode = (targetKey, fromCallback = false) => {
        if (!targetKey) return;
        const targetNode = findNode(targetKey);
        if (targetNode?.isActive) {
            dropNode(targetKey, false, fromCallback).then(shouldClose => {
                if (!shouldClose) {
                    return;
                }
                if (sortedTabNodesRef.current.length === 1) {
                    activeStartPage();
                } else {
                    activePrevNode(targetKey);
                }
            });
        } else {
            dropNode(targetKey, false, fromCallback).then(() => {
            });
        }
    };
    const refreshNode = (targetKey) => {
        if (!targetKey) return;
        refresh(targetKey).then(() => {
        });
    };
    const removeAllNodes = () => {
        let dropCount = 0;
        let isCurrentTabDropFail = false;
        const dropPromiseList = sortedTabNodesRef.current.map(node => {
            if (node.targetMenu?.isStartPage) {
                return Promise.resolve();
            }
            return dropNode(node.name ?? '').then((shouldClose) => {
                dropCount += shouldClose ? 1 : 0;
                if (!shouldClose && node.isActive) {
                    isCurrentTabDropFail = true;
                }
            });
        });
        Promise.all(dropPromiseList).then(() => {
            if (dropCount === sortedTabNodesRef.current.length) {
                // 全部关闭成功
                activeStartPage();
            } else if (!isCurrentTabDropFail) {
                // 没有全部关闭成功，但当前页面关闭成功，则退回上一个未关闭成功的页面
                activePrevNode(currentTabKey);
            }
        });
    };
    const removeOtherNodes = (targetKey) => {
        activeNode(targetKey);
        sortedTabNodesRef.current.forEach(node => {
            if (node.name !== targetKey) {
                dropNode(node.name ?? '').then(() => {
                });
            }
        });
    };
    const removeLeftSideNodes = (targetKey) => {
        for (let i = 0; i < sortedTabNodesRef.current.length; ++i) {
            const node = sortedTabNodesRef.current[i];
            if (node.name === targetKey) {
                return;
            }
            if (node.isActive) {
                activeNode(targetKey);
            }
            dropNode(node.name ?? '').then(() => {
            });
        }
    };
    const removeRightSideNodes = (targetKey) => {
        for (let i = sortedTabNodesRef.current.length - 1; i >= 0; --i) {
            const node = sortedTabNodesRef.current[i];
            if (node.name === targetKey) {
                return;
            }
            if (node.isActive) {
                activeNode(targetKey);
            }
            dropNode(node.name ?? '').then(() => {
            });
        }
    };
    const setTabNodeCallbacks = (targetKey, callbacks) => {
        if (!targetKey) {
            return;
        }
        tabNodeCallbacksMap.set(targetKey, callbacks);
    };

    const value: TabsContextType = {
        sortedTabNodes: sortedTabNodesRef.current,
        tabKeySequence,
        currentTabKey,
        currentTabNode,
        setTabKeySequence,
        setTabNodeAttributes,
        findNode,
        activeNode,
        removeNode,
        refreshNode,
        removeAllNodes,
        removeOtherNodes,
        removeLeftSideNodes,
        removeRightSideNodes,
        setTabNodeCallbacks
    };
    return (
        <TabsContext.Provider value={value}>
            {props.children}
        </TabsContext.Provider>
    );
}
