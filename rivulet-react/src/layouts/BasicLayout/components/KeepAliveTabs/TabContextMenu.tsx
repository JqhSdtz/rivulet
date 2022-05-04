import {Menu} from 'antd';
import {TabsContext, TabsContextType} from './TabsContextProvider';
import {TabNodeType} from './TabNodeProvider';
import {RefObject, useContext, useRef} from 'react';
import {useClickAway} from 'ahooks';
import {ItemType} from 'antd/lib/menu/hooks/useItems';

export default (props: {
    tabNode: TabNodeType,
    tabElemRef: RefObject<HTMLElement | null>,
    setContextMenuVisible: (visible: boolean) => void
}) => {
    const {
        tabNode,
        tabElemRef,
        setContextMenuVisible
    } = props;
    const {
        splitViewContainer,
        tabNodes,
        removeNode,
        refreshNode,
        setSplitViewOfTab,
        removeAllNodes,
        removeNodesOfSplitView,
        removeOtherNodes,
        removeLeftSideNodes,
        removeRightSideNodes
    } = useContext<TabsContextType>(TabsContext);
    const menuItems = [] as ItemType[];
    const notSinglePage = tabNodes.length > 1;
    const isStartPage = tabNode.targetMenu?.isStartPage;
    if (notSinglePage || !isStartPage) {
        menuItems.push({
            key: 'closeTab',
            label: '关闭',
            onClick: () => removeNode(tabNode.name)
        });
    }
    menuItems.push({
        key: 'refreshTab',
        label: '刷新',
        onClick: () => refreshNode(tabNode.name)
    });
    if (notSinglePage) {
        if (splitViewContainer.splitViews.length === 1) {
            menuItems.push({
                key: 'splitView',
                label: '分屏',
                onClick: () => setSplitViewOfTab(tabNode.name, 1)
            });
        } else {
            const splitViewMenuItems = [] as ItemType[];
            for (let i = 0; i < splitViewContainer.splitViews.length; ++i) {
                splitViewMenuItems.push({
                    key: 'splitView' + i,
                    label: '分屏' + (i + 1),
                    onClick: () => setSplitViewOfTab(tabNode.name, i)
                });
            }
            splitViewMenuItems.push({
                key: 'splitViewNew',
                label: '新的分屏',
                onClick: () => setSplitViewOfTab(tabNode.name, splitViewContainer.splitViews.length)
            });
            menuItems.push({
                key: 'splitView',
                label: '分屏',
                children: splitViewMenuItems
            })
        }
    }
    const batchCloseTabsChildren = [] as ItemType[];
    batchCloseTabsChildren.push({
        key: 'closeOtherTabs',
        label: '关闭其他',
        onClick: () => removeOtherNodes(tabNode.name)
    });
    if (!isStartPage) {
        batchCloseTabsChildren.push({
            key: 'closeAllTabs',
            label: '关闭全部',
            onClick: () => removeAllNodes()
        });
    }
    if (splitViewContainer.splitViews.length > 1) {
        batchCloseTabsChildren.push({
            key: 'removeNodesOfSplitView',
            label: '关闭分屏',
            onClick: () => removeNodesOfSplitView(tabNode.name, true)
        });
        batchCloseTabsChildren.push({
            key: 'removeNodesOfSplitViewMerge',
            label: '关闭分屏(合并)',
            onClick: () => removeNodesOfSplitView(tabNode.name, false)
        });
    }
    batchCloseTabsChildren.push({
        key: 'closeLeftSideTabs',
        label: '关闭左侧',
        onClick: () => removeLeftSideNodes(tabNode.name)
    });
    batchCloseTabsChildren.push({
        key: 'closeRightSideTabs',
        label: '关闭右侧',
        onClick: () => removeRightSideNodes(tabNode.name)
    });
    if (notSinglePage) {
        menuItems.push({
            key: 'batchCloseTabs',
            label: '批量关闭',
            children: batchCloseTabsChildren
        });
    }
    const ref = useRef<HTMLDivElement>(null);
    useClickAway((event) => {
        // 页签元素也在右键菜单之外，但不能在右键点页签元素时隐藏右键菜单
        // event.target有时候会是右键菜单外面的容器，此时也应该组织默认行为
        if (event.type === 'contextmenu'
            && (tabElemRef.current?.contains(event.target as any)
                || (event.target as HTMLDivElement)?.contains(ref.current))) {
            event.preventDefault();
            event.stopPropagation();
            return;
        }
        setContextMenuVisible(false);
    }, ref, ['tabClick', 'click', 'contextmenu']);
    return (
        <div ref={ref} onContextMenu={(event) => event.preventDefault()}>
            <Menu
                onClick={() => setContextMenuVisible(false)}
                selectable={false}
                items={menuItems}
            />
        </div>
    );
}
