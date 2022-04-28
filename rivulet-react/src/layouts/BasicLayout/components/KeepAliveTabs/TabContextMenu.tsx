import {Menu} from 'antd';
import {TabsContext, TabsContextType} from './TabsContextProvider';
import {TabNodeType} from './TabNodeProvider';
import {RefObject, useContext, useRef} from 'react';
import {useClickAway} from 'ahooks';
import {ItemType} from 'antd/lib/menu/hooks/useItems';

export default (props: {
    tabNode: TabNodeType,
    tabElemRef: RefObject<HTMLDivElement>,
    setContextMenuVisible: (visible: boolean) => void
}) => {
    const {
        tabNode,
        tabElemRef,
        setContextMenuVisible
    } = props;
    const {
        sortedTabNodes,
        removeNode,
        refreshNode,
        setSplitView,
        removeAllNodes,
        removeOtherNodes,
        removeLeftSideNodes,
        removeRightSideNodes
    } = useContext<TabsContextType>(TabsContext);
    const menuItems = [] as ItemType[];
    const notSinglePage = sortedTabNodes.length > 1;
    const isStartPage = tabNode.targetMenu?.isStartPage;
    const openNewBrowserTab = () => {
        removeNode(tabNode.name);
        window.open(tabNode.name);
    };
    if (notSinglePage) {
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
    menuItems.push({
        key: 'splitView',
        label: '分屏',
        onClick: () => setSplitView(tabNode.name, 1)
    });
    if (notSinglePage && !isStartPage) {
        menuItems.push({
            key: 'openNewBrowserTab',
            label: '新页面打开',
            onClick: openNewBrowserTab
        });
    }
    const batchCloseTabsChildren = [] as ItemType[];
    batchCloseTabsChildren.push({
        key: 'closeOtherTabs',
        label: '关闭其他',
        onClick: () => removeOtherNodes(tabNode.name)
    });
    if (notSinglePage && !isStartPage) {
        batchCloseTabsChildren.push({
            key: 'closeAllTabs',
            label: '关闭全部',
            onClick: () => removeAllNodes()
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
    menuItems.push({
        key: 'batchCloseTabs',
        label: '批量关闭',
        children: batchCloseTabsChildren
    });
    const ref = useRef<HTMLDivElement>(null);
    useClickAway((event) => {
        // 页签元素也在右键菜单之外，但不能在右键点页签元素时隐藏右键菜单
        if (event.type === 'contextmenu' && tabElemRef.current?.contains(event.target as any)) {
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
