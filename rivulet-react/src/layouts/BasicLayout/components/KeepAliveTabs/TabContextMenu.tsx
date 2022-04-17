import {Menu} from 'antd';
import {CachingNodeController} from './cachingNodeController';
import {CachingNodeType} from './CachingNode';
import {ReactElement, RefObject, useRef} from 'react';
import {useClickAway} from 'ahooks';

export default (props: {
    cachingNodeController: CachingNodeController,
    cachingNode: CachingNodeType,
    tabElemRef: RefObject<HTMLDivElement>,
    setContextMenuVisible: (visible: boolean) => void
}) => {
    const {
        cachingNodeController,
        cachingNode,
        tabElemRef,
        setContextMenuVisible
    } = props;
    const {
        sortedCachingNodes,
        removeNode,
        refreshNode,
        removeOtherNodes,
        removeLeftSideNodes,
        removeRightSideNodes
    } = cachingNodeController;
    const menuItems = [] as ReactElement[];
    if (sortedCachingNodes.length > 1) {
        menuItems.push(
            <Menu.Item key="closeTab" onClick={() => removeNode(cachingNode.name)}>
                关闭
            </Menu.Item>
        );
    }
    menuItems.push(
        <Menu.Item key="refreshTab" onClick={() => refreshNode(cachingNode.name)}>
            刷新
        </Menu.Item>
    );
    menuItems.push(
        <Menu.SubMenu key="batchCloseTabs" title="批量关闭">
            <Menu.Item key="closeOtherTabs" onClick={() => removeOtherNodes(cachingNode.name)}>
                关闭其他
            </Menu.Item>
            <Menu.Item key="closeLeftSideTabs" onClick={() => removeLeftSideNodes(cachingNode.name)}>
                关闭左侧
            </Menu.Item>
            <Menu.Item key="closeRightSideTabs" onClick={() => removeRightSideNodes(cachingNode.name)}>
                关闭右侧
            </Menu.Item>
        </Menu.SubMenu>
    );
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
            <Menu onClick={() => setContextMenuVisible(false)}>
                {menuItems}
            </Menu>
        </div>
    );
}
