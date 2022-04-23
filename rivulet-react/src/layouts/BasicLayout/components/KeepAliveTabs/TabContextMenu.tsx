import {Menu} from 'antd';
import {TabsContext, TabsContextType} from './TabsContextProvider';
import {TabNodeType} from './TabNodeProvider';
import {ReactElement, RefObject, useContext, useRef} from 'react';
import {useClickAway} from 'ahooks';

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
        removeAllNodes,
        removeOtherNodes,
        removeLeftSideNodes,
        removeRightSideNodes
    } = useContext<TabsContextType>(TabsContext);
    const menuItems = [] as ReactElement[];
    const notSinglePage = sortedTabNodes.length > 1;
    const isStartPage = tabNode.targetMenu?.isStartPage;
    const openNewBrowserTab = () => {
        removeNode(tabNode.name);
        window.open(tabNode.name);
    }
    if (notSinglePage) {
        menuItems.push(
            <Menu.Item key="closeTab" onClick={() => removeNode(tabNode.name)}>
                关闭
            </Menu.Item>
        );
    }
    menuItems.push(
        <Menu.Item key="refreshTab" onClick={() => refreshNode(tabNode.name)}>
            刷新
        </Menu.Item>
    );
    if (notSinglePage && !isStartPage) {
        menuItems.push(
            <Menu.Item key="openNewBrowserTab" onClick={openNewBrowserTab}>
                新页面打开
            </Menu.Item>
        );
    }
    menuItems.push(
        <Menu.SubMenu key="batchCloseTabs" title="批量关闭">
            <Menu.Item key="closeOtherTabs" onClick={() => removeOtherNodes(tabNode.name)}>
                关闭其他
            </Menu.Item>
            {
                notSinglePage && !isStartPage &&
                <Menu.Item key="closeAllTabs" onClick={() => removeAllNodes()}>
                    关闭全部
                </Menu.Item>
            }
            <Menu.Item key="closeLeftSideTabs" onClick={() => removeLeftSideNodes(tabNode.name)}>
                关闭左侧
            </Menu.Item>
            <Menu.Item key="closeRightSideTabs" onClick={() => removeRightSideNodes(tabNode.name)}>
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
            <Menu
                onClick={() => setContextMenuVisible(false)}
                selectable={false}
            >
                {menuItems}
            </Menu>
        </div>
    );
}
