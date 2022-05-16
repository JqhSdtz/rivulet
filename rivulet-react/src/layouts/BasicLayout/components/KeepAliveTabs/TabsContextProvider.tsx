import {TabNodeAttributes, TabNodeCallbacks, TabNodeType} from './TabNodeProvider';
import {useHistory, useLocation} from 'ice';
import {useAliveController} from 'react-activation';
import React, {MutableRefObject, ReactElement, useContext, useRef} from 'react';
import {RouteContext} from '@/layouts/BasicLayout';
import {defaultStartPage, MenuConfigItem} from '@/menuConfig';
import {useCreation, useEventEmitter, useLatest, useUpdate} from 'ahooks';
import {nanoid} from 'nanoid';
import RvUtil from '@/utils/rvUtil';
import {EventEmitter} from 'ahooks/lib/useEventEmitter';

type AttributeType = {
    splitView?: SplitViewType
} & TabNodeAttributes & TabNodeCallbacks;

const temporaryTabNodesMap = new Map<string, TabNodeType>();
const tabNodeAttributeMap = new Map<string, AttributeType>();

function refreshTabNode(targetNode: TabNodeType | undefined) {
    if (!targetNode) return;
    const tabAttributes = tabNodeAttributeMap.get(targetNode.name ?? '');
    if (tabAttributes) {
        for (const [key, value] of Object.entries(tabAttributes)) {
            targetNode[key] = value;
        }
    }
}

function setTabNodeAttributes(targetKey: string | undefined, attributes: AttributeType) {
    if (!targetKey) return;
    const prevAttr = tabNodeAttributeMap.get(targetKey);
    tabNodeAttributeMap.set(targetKey, {
        ...prevAttr,
        ...attributes
    });
}

function transferTemporaryTabNode(cachingNodes: TabNodeType[]) {
    const latestCachingNode = cachingNodes[cachingNodes.length - 1];
    if (!latestCachingNode || latestCachingNode._temporary) return;
    const temporaryTabNode = temporaryTabNodesMap.get(latestCachingNode.name ?? '');
    if (!temporaryTabNode) return;
    temporaryTabNode._temporary = false;
    RvUtil.mergeObject(latestCachingNode, temporaryTabNode, true);
    const splitView = temporaryTabNode.splitView;
    splitView.tabNodes.forEach((tabNode, index) => {
        if (tabNode.name === cachingNodes[0].name) {
            splitView.tabNodes[index] = cachingNodes[0];
        }
    });
    temporaryTabNodesMap.delete(latestCachingNode.name ?? '');
}

function preProcessCachingNodes(cachingNodes: TabNodeType[], splitViewContainer: SplitViewContainerType, currentTabKey: string): TabNodeType[] {
    // 先去重，此处有待进一步考虑
    cachingNodes = cachingNodes.filter((node, index, arr) => {
        return arr.map(mapNode => mapNode.name).indexOf(node.name) === index;
    });
    const activeSplitView = splitViewContainer.splitViews.find(view => view.isActive) ?? splitViewContainer.splitViews[0];
    // 还没有KeepAlive节点被渲染，则根据当前的url判断当前要打开哪个页面，提前加入一个CachingNode节点
    // 防止出现没有KeepAlive就没有CachingNode，没有CachingNode就渲染不了KeepAlive的死循环
    if (cachingNodes.length === 0 || cachingNodes[cachingNodes.length - 1].name !== currentTabKey) {
        const temporaryNode = {
            _temporary: true,
            createTime: Date.now(),
            updateTime: Date.now(),
            name: currentTabKey,
            id: currentTabKey,
            isActive: true,
            splitView: activeSplitView
        };
        cachingNodes.push(temporaryNode);
        temporaryTabNodesMap.set(temporaryNode.name, temporaryNode);
    }
    // 将临时创建的tabNode的属性转移到正式的tabNode上
    transferTemporaryTabNode(cachingNodes);
    // 将tabNode的属性刷新到tabNode对象中
    cachingNodes.forEach(node => refreshTabNode(node));
    return cachingNodes;
}

function matchMenuConfig(menuItems: MenuConfigItem[], node: TabNodeType) {
    if (node.targetMenu) {
        return;
    }
    for (let i = 0; i < menuItems.length; ++i) {
        const menu = menuItems[i];
        if (menu.testPath?.(node?.name) ?? false) {
            setTabNodeAttributes(node.name, {
                targetMenu: menu
            });
            return;
        }
        if (menu.routes) {
            matchMenuConfig(menu.routes as MenuConfigItem[], node);
        }
    }
}

interface TabNodeOperations {
    findNode(targetKey: string | undefined): TabNodeType | undefined;

    activeNode(targetKey: string | undefined): void;

    removeNode(targetKey: string | undefined): void;

    refreshNode(targetKey: string | undefined): void;

    removeAllNodes(): void;

    removeNodesOfSplitView(targetKey: string | undefined, isMerge: boolean): void;

    removeOtherNodes(targetKey): void;

    removeLeftSideNodes(targetKey): void;

    removeRightSideNodes(targetKey): void;

    setSplitViewOfTab(targetKey: string | undefined, index: number): void;

    setTabNodeCallbacks(targetKey: string | undefined, callbacks: TabNodeCallbacks): void;

    setTabNodeAttributes(targetKey: string | undefined, attributes: AttributeType): void;

    refreshTabNode(targetNode: TabNodeType | undefined): void;
}

export interface SplitViewType {
    id: string,
    isActive?: boolean;
    tabBarElement?: ReactElement;
    tabNodes: TabNodeType[];
}

export interface SplitViewContainerType {
    splitViews: SplitViewType[];
}

export type TabsContextType = {
    tabsEvent: EventEmitter<any>;
    tabNodes: TabNodeType[];
    currentTabKey: string;
    currentTabNode: TabNodeType | undefined;
    splitViewContainer: SplitViewContainerType;
    keepAliveTabsElemRef: MutableRefObject<HTMLDivElement>;
    updateTabs(): void;
    getSplitViewContainerCopy(): SplitViewContainerType;
    resetSplitViewContainer(splitViewContainer: SplitViewContainerType): void;
    resetSplitViewOfTabNodes(): void;
    removeSplitView(splitViewId: string): void;
} & TabNodeOperations;

export const TabsContext = React.createContext({} as TabsContextType);

export default (props) => {
    const {getCachingNodes, drop, refresh} = useAliveController();
    const {menuData} = useContext(RouteContext);
    const {pathname, search} = useLocation();
    const history = useHistory();
    const currentTabKey = pathname + search;
    const tabsEvent = useEventEmitter();
    const updateTabs: () => void = useUpdate();
    const initSplitView = useCreation<SplitViewType>(() => {
        return {
            tabNodes: [],
            id: nanoid(),
            isActive: true
        };
    }, []);
    const splitViewContainerRef = useRef<SplitViewContainerType>({
        splitViews: [initSplitView]
    });
    const splitViewContainer = splitViewContainerRef.current;
    const getSplitViewContainerCopy = () => {
        const splitViewContainerCopy = {...splitViewContainer};
        splitViewContainerCopy.splitViews = [];
        for (let i = 0; i < splitViewContainer.splitViews.length; ++i) {
            const splitView = splitViewContainer.splitViews[i];
            const newSplitView = {...splitView};
            newSplitView.tabNodes = [...splitView.tabNodes];
            splitViewContainerCopy.splitViews.push(newSplitView);
        }
        return splitViewContainerCopy;
    };
    const resetSplitViewOfTabNodes = () => splitViewContainerRef.current.splitViews.forEach(
        splitView => splitView.tabNodes.forEach(node => setTabNodeAttributes(node.name, {splitView}))
    );
    const resetSplitViewContainer = (splitViewContainerCopy) => {
        splitViewContainerRef.current = splitViewContainerCopy;
        resetSplitViewOfTabNodes();
    };
    const lastTabNodesRef = useRef<TabNodeType[]>([]);
    const tabNodes = preProcessCachingNodes(getCachingNodes(), splitViewContainer, currentTabKey);
    const lastTabNodes = lastTabNodesRef.current;
    lastTabNodesRef.current = [...tabNodes];
    // 因为涉及回调，可能导致闭包问题，所以使用useLatest包裹
    const tabNodesRef = useLatest(tabNodes);
    const findNode = targetKey => tabNodesRef.current.find(node => node.name === targetKey) as TabNodeType | undefined;
    // 将splitViewContainer中的tabNode同步为getCachingNodes获取的node对象，以实现同步更新内容
    splitViewContainer.splitViews.forEach(splitView => {
        for (let i = 0; i < splitView.tabNodes.length; ++i) {
            const tabNode = findNode(splitView.tabNodes[i].name);
            if (tabNode) {
                splitView.tabNodes[i] = tabNode;
            }
        }
    });
    // 筛选出这一次有，上一次没有的节点，记为新增加的节点
    const newTabNodes = tabNodes.filter(node1 => {
        node1.isNewTab = !lastTabNodes.some(node2 => node2.name === node1.name);
        return node1.isNewTab;
    });
    const currentTabNode = findNode(currentTabKey) ?? {} as TabNodeType;
    const removeTabNodeFromSplitView = targetNode => {
        targetNode.splitView.tabNodes = targetNode.splitView.tabNodes.filter(node => node.name !== targetNode.name);
        if (targetNode.splitView.tabNodes.length === 0) {
            removeSplitView(targetNode.splitView.id);
        }
    };
    const curSplitView = currentTabNode.splitView;
    if (newTabNodes.length > 0) {
        curSplitView.tabNodes.forEach(node => setTabNodeAttributes(node.name, {isActive: false}));
        newTabNodes.forEach(tabNode => {
            setTabNodeAttributes(tabNode.name, {
                splitView: curSplitView,
                isActive: tabNode.name === currentTabKey
            });
            curSplitView.tabNodes.push(tabNode);
        });
    }
    tabNodes.forEach(tabNode => {
        // 因为菜单数据可能延迟加载，所以每次都要全部更新一次对应菜单
        matchMenuConfig(menuData as MenuConfigItem[], tabNode);
        refreshTabNode(tabNode);
    });
    const setActiveSplitView = splitView => {
        splitViewContainer.splitViews.forEach(splitView => splitView.isActive = false);
        splitView.isActive = true;
    };
    const setSplitViewOfTab = (targetKey, index) => {
        const targetNode = findNode(targetKey);
        if (!targetNode) return;
        const splitViews = splitViewContainer.splitViews;
        if (!splitViews[index]) {
            splitViews[index] = {
                id: nanoid(),
                tabNodes: []
            };
        }
        splitViews[index].tabNodes.push(targetNode);
        if (targetNode.splitView) {
            // 将tabNode从原来的splitView中移除
            removeTabNodeFromSplitView(targetNode);
        }
        setTabNodeAttributes(targetNode.name, {
            splitView: splitViews[index]
        });
        setActiveSplitView(targetNode.splitView);
        updateTabs();
    };
    const activeNode = (targetKey) => {
        if (!targetKey) return;
        const targetNode = findNode(targetKey) ?? {} as TabNodeType;
        setActiveSplitView(targetNode.splitView);
        history.push(targetKey);
    };
    const activePrevNode = (targetKey) => {
        const splitView = findNode(targetKey)?.splitView;
        if (!splitView) return;
        const curIdx = splitView.tabNodes.findIndex(
            node => node.name === targetKey
        );
        if (curIdx === 0) {
            activeNode(splitView.tabNodes[1].name ?? '');
        } else if (curIdx > 0) {
            activeNode(splitView.tabNodes[curIdx - 1].name ?? '');
        }
    };
    const activeStartPage = () => activeNode(defaultStartPage);
    const dropNode = async (targetKey, dropSync = false, fromCallback = false) => {
        const targetNode = findNode(targetKey);
        if (!targetNode) return;
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
            // 将tabNode从原来的splitView中移除
            removeTabNodeFromSplitView(targetNode);
        };
        if (fromCallback) {
            await doDrop();
            return true;
        }
        const clearAttention = () => {
            setTabNodeAttributes(targetNode.name, {
                needAttention: false
            });
            updateTabs();
        };
        if (targetNode.beforeCloseCallback) {
            const shouldClose = await targetNode.beforeCloseCallback(clearAttention, doClose);
            if (shouldClose) {
                await doDrop();
            } else {
                setTabNodeAttributes(targetNode.name, {
                    needAttention: true
                });
                updateTabs();
                return false;
            }
        } else {
            await doDrop();
        }
        return true;
    };
    const removeSplitView = (splitViewId: string, shouldUpdateTabs: boolean = true) => {
        splitViewContainer.splitViews = splitViewContainer.splitViews.filter(view => view.id !== splitViewId);
        const curSplitViewIndex = splitViewContainer.splitViews.findIndex(view => view.id === splitViewId);
        const prevSplitViewIndex = curSplitViewIndex === 0 ? 1 : curSplitViewIndex - 1;
        const prevSplitView = splitViewContainer.splitViews[prevSplitViewIndex];
        const curSplitView = splitViewContainer.splitViews[curSplitViewIndex];
        if (curSplitView && prevSplitView) {
            curSplitView.isActive = false;
            prevSplitView.isActive = true;
        }
        if (shouldUpdateTabs) {
            updateTabs();
        }
    };
    const removeNode = (targetKey, fromCallback = false) => {
        const targetNode = findNode(targetKey);
        if (!targetNode) return;
        if (targetNode.isActive) {
            dropNode(targetKey, false, fromCallback).then(shouldClose => {
                if (!shouldClose) {
                    return;
                }
                if (tabNodesRef.current.length === 1) {
                    activeStartPage();
                } else if (targetNode.splitView.tabNodes.length === 1) {
                    removeSplitView(targetNode.splitView.id);
                } else {
                    activePrevNode(targetKey);
                }
            });
        } else {
            dropNode(targetKey, false, fromCallback).then(() => {
                if (targetNode.splitView.tabNodes.length === 1) {
                    removeSplitView(targetNode.splitView.id);
                }
            });
        }
    };
    const refreshNode = (targetKey) => {
        if (!targetKey) return;
        refresh(targetKey).then(() => {
        });
    };
    const removeMultiNodes = (tabNodes: TabNodeType[], onAllSuccess: () => void) => {
        let dropCount = 0;
        let isCurrentTabDropFail = false;
        const dropPromiseList = tabNodes.map(node => {
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
            if (dropCount === tabNodes.length) {
                // 全部关闭成功
                onAllSuccess();
            } else if (!isCurrentTabDropFail) {
                // 没有全部关闭成功，但当前页面关闭成功，则退回上一个未关闭成功的页面
                activePrevNode(currentTabKey);
            }
        });
    };
    const removeAllNodes = () => {
        removeMultiNodes(tabNodesRef.current, activeStartPage);
    };
    const removeNodesOfSplitView = (targetKey: string, isMerge: boolean) => {
        const targetNode = findNode(targetKey);
        if (!targetNode) return;
        const targetSplitView = targetNode.splitView;
        if (isMerge) {
            const splitViewIndex = splitViewContainer.splitViews.findIndex(view => view.id === targetSplitView.id);
            const prevSplitViewIndex = splitViewIndex === 0 ? 1 : splitViewIndex - 1;
            const prevSplitView = splitViewContainer.splitViews[prevSplitViewIndex];
            targetSplitView.tabNodes.forEach(node => setTabNodeAttributes(node.name, {splitView: prevSplitView}));
            removeSplitView(targetSplitView.id);
        } else {
            removeMultiNodes(targetSplitView.tabNodes, () => {
                removeSplitView(targetSplitView.id);
            });
        }
    };
    const removeOtherNodes = (targetKey) => {
        const targetNode = findNode(targetKey);
        if (!targetNode) return;
        activeNode(targetKey);
        targetNode.splitView.tabNodes.forEach(node => {
            if (node.name !== targetKey) {
                dropNode(node.name ?? '').then(() => {
                });
            }
        });
    };
    const removeLeftSideNodes = (targetKey) => {
        const targetNode = findNode(targetKey);
        if (!targetNode) return;
        for (let i = 0; i < targetNode.splitView.tabNodes.length; ++i) {
            const node = targetNode.splitView.tabNodes[i];
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
        const targetNode = findNode(targetKey);
        if (!targetNode) return;
        for (let i = targetNode.splitView.tabNodes.length - 1; i >= 0; --i) {
            const node = targetNode.splitView.tabNodes[i];
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
        setTabNodeAttributes(targetKey, callbacks);
        refreshTabNode(findNode(targetKey));
    };

    const keepAliveTabsElemRef = useRef<HTMLDivElement>(null as unknown as HTMLDivElement);
    const value: TabsContextType = {
        keepAliveTabsElemRef,
        tabsEvent,
        tabNodes: tabNodesRef.current,
        splitViewContainer,
        currentTabKey,
        currentTabNode,
        findNode,
        activeNode,
        removeNode,
        refreshNode,
        removeAllNodes,
        removeNodesOfSplitView,
        removeOtherNodes,
        removeLeftSideNodes,
        removeRightSideNodes,
        updateTabs,
        setSplitViewOfTab,
        setTabNodeCallbacks,
        setTabNodeAttributes,
        refreshTabNode,
        getSplitViewContainerCopy,
        resetSplitViewOfTabNodes,
        resetSplitViewContainer,
        removeSplitView: splitViewId => removeSplitView(splitViewId, false)
    };
    return (
        <TabsContext.Provider value={value}>
            {props.children}
        </TabsContext.Provider>
    );
}
