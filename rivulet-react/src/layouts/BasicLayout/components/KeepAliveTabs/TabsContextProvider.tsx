import {TabNodeCallbacks, TabNodeType, TabStatus} from './TabNodeProvider';
import {useHistory, useLocation} from 'ice';
import {useAliveController} from 'react-activation';
import React, {useContext, useState} from 'react';
import {RouteContext} from '@/layouts/BasicLayout';
import {defaultStartPage, MenuConfigItem} from '@/layouts/BasicLayout/configs/menuConfig';
import {useCreation, useMap} from 'ahooks';

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

function fillCachingNodeWithMenuDataAndStatus(sortedTabNodes: TabNodeType[], menuData: MenuConfigItem[], tabStatusMap: Map<string, TabStatus>) {
    sortedTabNodes.forEach(node => {
        if (!node.targetMenu) {
            matchMenuConfig(menuData, node);
        }
        const tabStatus = tabStatusMap.get(node.name ?? '');
        if (tabStatus) {
            node.needAttention = tabStatus.needAttention;
        }
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

interface TabNodeOperations {
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
    setTabKeySequence: (tabKeySequence: string[]) => void;
    currentTabKey: string;
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
    const [tabStatusMap, tabStatusHandler] = useMap<string, TabStatus>([]);
    let [tabKeySequence, setTabKeySequence] = useState([] as string[]);
    const tabNodeCallbacksMap = useCreation(() => new Map<string, TabNodeCallbacks>(), []);
    // 将cachingNodes中的增加和删除反映到tabKeySequence中
    tabKeySequence = synchronizeTabKeySequence(tabKeySequence, cachingNodes);
    // 对cachingNodes进行排序
    const sortedTabNodes = sortCachingNodes(tabKeySequence, cachingNodes);
    // 设置cachingNode对应的MenuItem
    fillCachingNodeWithMenuDataAndStatus(sortedTabNodes, menuData as MenuConfigItem[], tabStatusMap);
    const activeNode = (targetKey) => {
        if (!targetKey) return;
        history.push(targetKey ?? '');
    };
    const activePrevNode = (targetKey) => {
        const curIdx = sortedTabNodes.findIndex(
            node => node.name === targetKey
        );
        if (curIdx === 0) {
            activeNode(sortedTabNodes[1].name ?? '');
        } else if (curIdx > 0) {
            activeNode(sortedTabNodes[curIdx - 1].name ?? '');
        }
    };
    const activeStartPage = () => activeNode(defaultStartPage);
    const dropNode = async (targetKey, dropSync = false, ignoreCallback = false) => {
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
        if (ignoreCallback) {
            await doDrop();
            return true;
        }
        const callbacks = tabNodeCallbacksMap.get(targetKey);
        const prevStatus = tabStatusHandler.get(targetKey);
        const clearAttention = () => {
            tabStatusHandler.set(targetKey, {
                ...prevStatus,
                needAttention: false
            });
        };
        if (callbacks?.beforeCloseCallback) {
            const shouldClose = await callbacks.beforeCloseCallback(clearAttention, doClose);
            if (shouldClose) {
                await doDrop();
            } else {
                tabStatusHandler.set(targetKey, {
                    ...prevStatus,
                    needAttention: true
                });
                return false;
            }
        } else {
            await doDrop();
        }
        return true;
    };
    const removeNode = (targetKey, ignoreCallback = false) => {
        if (!targetKey) return;
        const isActive = targetKey === currentTabKey;
        if (isActive) {
            dropNode(targetKey, false, ignoreCallback).then(shouldClose => {
                if (!shouldClose) {
                    return;
                }
                if (sortedTabNodes.length === 1) {
                    activeStartPage();
                } else {
                    activePrevNode(targetKey);
                }
            });
        } else {
            dropNode(targetKey, false, ignoreCallback).then(() => {
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
        const dropPromiseList = sortedTabNodes.map(node => {
            if (node.targetMenu?.isStartPage) {
                return Promise.resolve();
            }
            return dropNode(node.name ?? '').then((shouldClose) => {
                dropCount += shouldClose ? 1 : 0;
                if (!shouldClose && node.name === currentTabKey) {
                    isCurrentTabDropFail = true;
                }
            });
        });
        Promise.all(dropPromiseList).then(() => {
            if (dropCount === sortedTabNodes.length) {
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
        sortedTabNodes.forEach(node => {
            if (node.name !== targetKey) {
                dropNode(node.name ?? '').then(() => {
                });
            }
        });
    };
    const removeLeftSideNodes = (targetKey) => {
        for (let i = 0; i < sortedTabNodes.length; ++i) {
            const node = sortedTabNodes[i];
            if (node.name === targetKey) {
                return;
            }
            if (node.name === currentTabKey) {
                activeNode(targetKey);
            }
            dropNode(node.name ?? '').then(() => {
            });
        }
    };
    const removeRightSideNodes = (targetKey) => {
        for (let i = sortedTabNodes.length - 1; i >= 0; --i) {
            const node = sortedTabNodes[i];
            if (node.name === targetKey) {
                return;
            }
            if (node.name === currentTabKey) {
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
        sortedTabNodes,
        tabKeySequence,
        setTabKeySequence,
        currentTabKey,
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
