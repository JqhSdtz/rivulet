import RvUtil from '@/utils/rvUtil';
import {TabNodeType} from './TabNodeProvider';
import {
    Dispatch,
    MouseEvent,
    MutableRefObject,
    ReactElement,
    SetStateAction,
    useContext,
    useRef,
    useState
} from 'react';
import {CSS} from '@dnd-kit/utilities';
import {TabsContext, TabsContextType} from './TabsContextProvider';
import {Dropdown} from 'antd';
import {useSortable} from '@dnd-kit/sortable';
import {ItemType} from 'antd/lib/menu/hooks/useItems';
import {useClickAway} from 'ahooks';

function getDropdownMenuItems(tabNode: TabNodeType, tabsContext: TabsContextType) {
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
    } = tabsContext;
    const menuItems = [] as ItemType[];
    const notSinglePage = tabNodes.length > 1;
    menuItems.push({
        key: 'closeTab',
        label: '关闭',
        onClick: () => removeNode(tabNode.name)
    });
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
            });
        }
    }
    const batchCloseTabsChildren = [] as ItemType[];
    batchCloseTabsChildren.push({
        key: 'closeOtherTabs',
        label: '关闭其他',
        onClick: () => removeOtherNodes(tabNode.name)
    });
    batchCloseTabsChildren.push({
        key: 'closeAllTabs',
        label: '关闭全部',
        onClick: () => removeAllNodes()
    });
    if (splitViewContainer.splitViews.length > 1) {
        batchCloseTabsChildren.push({
            key: 'removeNodesOfSplitView',
            label: '关闭分屏',
            onClick: () => removeNodesOfSplitView(tabNode.name, false)
        });
        batchCloseTabsChildren.push({
            key: 'removeNodesOfSplitViewMerge',
            label: '关闭分屏(合并)',
            onClick: () => removeNodesOfSplitView(tabNode.name, true)
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
    return menuItems;
}


interface TabDividerProps {
    isShow: boolean;
}

const TabDivider = (props: TabDividerProps) => {
    let tmpClassName = 'keep-alive-tab-divider';
    if (!props.isShow) {
        tmpClassName += ' keep-alive-tab-divider-hidden';
    }
    return <div className={tmpClassName}/>;
};

const SortableTabNode = (props: {
    className: string;
    onMouseEnter: () => void;
    onMouseLeave: () => void;
    showBeforeDivider: boolean;
    showAfterDivider: boolean;
    tabNodeElem: ReactElement;
    tabNode: TabNodeType;
}) => {
    const {
        tabNode,
        tabNodeElem,
        className,
        onMouseEnter,
        onMouseLeave,
        showAfterDivider,
        showBeforeDivider
    } = props;
    const sortableProps = {
        id: tabNode.name ?? '',
        data: {
            type: 'tabNode'
        }
    };
    const tabsContext = useContext<TabsContextType>(TabsContext);
    const {
        tabsEvent,
        setTabNodeAttributes,
        refreshTabNode
    } = tabsContext;
    const sortableAttr = useSortable(sortableProps);
    const {
        attributes,
        listeners,
        node: tabElemRef,
        setNodeRef,
        transform,
        isDragging
    } = sortableAttr;
    setTabNodeAttributes(tabNode.name, {
        tabElement: tabNodeElem,
        sortableAttr
    });
    refreshTabNode(tabNode);
    const [contextMenuVisible, setContextMenuVisible] = useState(false);
    const onContextMenu = (event: MouseEvent) => {
        event.preventDefault();
        event.stopPropagation();
        setContextMenuVisible(true);
        tabsEvent.emit({
            type: 'openContextMenu',
            tabNode
        });
    };
    tabsEvent.useSubscription(val => {
        if (val.type !== 'openContextMenu') return;
        // 打开其他tabNode的contextmenu时，关闭当前的contextmenu
        if (val.tabNode.name !== tabNode.name) {
            setContextMenuVisible(false);
        }
    });
    // 没有找到antd的dropdown组件禁用关闭时的过渡效果的方法，应该是通过js控制的，
    // 所以采用直接添加class，直接设置display为none的方式实现立即关闭右键菜单
    let overlayClassName = 'keep-alive-tab-context-menu';
    if (!contextMenuVisible) overlayClassName += ' tab-context-menu-hidden';
    const sortableClassName = isDragging ? ' keep-alive-tab-dragged' : '';
    const sortableStyle = {
        transform: CSS.Transform.toString(transform),
        transition: 'transform 0.3s cubic-bezier(0.645, 0.045, 0.355, 1)'
    };
    const tabContextMenuItems = getDropdownMenuItems(tabNode, tabsContext);
    return (
        <Dropdown
            menu={{items: tabContextMenuItems, onClick: () => setContextMenuVisible(false)}}
            trigger={['contextMenu']}
            overlayClassName={overlayClassName}
            open={contextMenuVisible}
            onOpenChange={setContextMenuVisible}
            destroyPopupOnHide
        >
            <div className={className + sortableClassName}
                 key={tabNode.name}
                 ref={setNodeRef}
                 style={sortableStyle}
                 {...attributes}
                 {...listeners}
                 onMouseEnter={onMouseEnter}
                 onMouseLeave={onMouseLeave}
                 onContextMenu={onContextMenu}
            >
                <TabDivider isShow={showBeforeDivider}/>
                {tabNodeElem}
                <TabDivider isShow={showAfterDivider}/>
            </div>
        </Dropdown>
    );
};

interface TabNodeWrapperProps {
    prevTabNode: MutableRefObject<ReactElement>;
    currentMouseOverNodeState: [any, Dispatch<SetStateAction<any>>];
}

const isSameTab = (tabNode1, tabNode2) => RvUtil.equalAndNotEmpty(tabNode1?.key, tabNode2?.key);

export default ({
                    prevTabNode,
                    currentMouseOverNodeState
                }: TabNodeWrapperProps) => {
    return (tabNodeElem) => {
        const {
            findNode
        } = useContext<TabsContextType>(TabsContext);
        const [currentMouseOverNode, setCurrentMouseOverNode] = currentMouseOverNodeState;
        const targetNode = findNode(tabNodeElem.key) ?? {} as TabNodeType;
        const indexInSplitView = targetNode.splitView.tabNodes.findIndex(node => node?.name === tabNodeElem.key);
        let className = 'keep-alive-tab';
        const isFirst = indexInSplitView === 0;
        const isLast = indexInSplitView === targetNode.splitView.tabNodes.length - 1;
        const isActive = targetNode.isActive;
        if (indexInSplitView === -1) {
            className += ' keep-alive-loading-tab';
        } else {
            if (isFirst) {
                className += ' keep-alive-first-tab';
            }
            if (isLast) {
                className += ' keep-alive-last-tab';
            }
            if (isActive) {
                className += ' keep-alive-active-tab';
            }
            if (targetNode?.needAttention) {
                className += ' keep-alive-need-attention-tab';
            }
        }
        let showBeforeDivider = !isFirst;
        let showAfterDivider = isLast && !isFirst;
        if (isActive || isSameTab(currentMouseOverNode, tabNodeElem)) {
            showBeforeDivider = false;
            showAfterDivider = false;
        }
        if (findNode(prevTabNode.current?.key as string ?? '')?.isActive || isSameTab(currentMouseOverNode, prevTabNode.current)) {
            showBeforeDivider = false;
        }
        prevTabNode.current = tabNodeElem;
        const onMouseEnter = () => {
            setCurrentMouseOverNode(tabNodeElem);
        };
        const onMouseLeave = () => {
            setCurrentMouseOverNode(null);
        };
        return (
            <SortableTabNode
                className={className}
                onMouseEnter={onMouseEnter}
                onMouseLeave={onMouseLeave}
                showBeforeDivider={showBeforeDivider}
                showAfterDivider={showAfterDivider}
                tabNodeElem={tabNodeElem}
                tabNode={targetNode}
            />
        );
    };
}
