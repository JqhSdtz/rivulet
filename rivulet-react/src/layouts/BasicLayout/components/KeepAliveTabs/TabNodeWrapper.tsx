import RvUtil from '@/utils/rvUtil';
import {CachingNodeType} from './TabNodeProvider';
import {
    Dispatch,
    MouseEvent,
    MutableRefObject,
    ReactElement,
    SetStateAction,
    useContext,
    useRef,
    useState,
} from 'react';
import {SortableElement} from 'react-sortable-hoc';
import {TabsContext, TabsContextType} from './TabsContextProvider';
import TabContextMenu from './TabContextMenu';
import {Dropdown} from 'antd';

interface TabDividerProps {
    isShow: boolean
}

const TabDivider = (props: TabDividerProps) => {
    let tmpClassName = 'keep-alive-tab-divider';
    if (!props.isShow) {
        tmpClassName += ' keep-alive-tab-divider-hidden';
    }
    return <div className={tmpClassName}/>;
};

const SortableTabNode = SortableElement((props: {
    className: string,
    onMouseEnter: () => void,
    onMouseLeave: () => void,
    showBeforeDivider: boolean,
    showAfterDivider: boolean,
    tabNode: ReactElement,
    cachingNode: CachingNodeType
}) => {
    const [contextMenuVisible, setContextMenuVisible] = useState(false);
    const onContextMenu = (event: MouseEvent) => {
        event.preventDefault();
        setContextMenuVisible(true);
    }
    const tabElemRef = useRef<HTMLDivElement>(null);
    const tabContextMenu = (
        <TabContextMenu
            cachingNode={props.cachingNode}
            tabElemRef={tabElemRef}
            setContextMenuVisible={setContextMenuVisible}
        />
    );
    // 没有找到antd的dropdown组件禁用关闭时的过渡效果的方法，应该是通过js控制的，
    // 所以采用直接添加class，直接设置display为none的方式实现立即关闭右键菜单
    let overlayClassName = 'keep-alive-tab-context-menu';
    if (!contextMenuVisible) overlayClassName += ' tab-context-menu-hidden';
    return (
        <Dropdown
            overlay={tabContextMenu}
            trigger={['contextMenu']}
            overlayClassName={overlayClassName}
            visible={contextMenuVisible}
            destroyPopupOnHide
        >
            <div className={props.className}
                 ref={tabElemRef}
                 onMouseEnter={props.onMouseEnter}
                 onMouseLeave={props.onMouseLeave}
                 onContextMenu={onContextMenu}
            >
                <TabDivider isShow={props.showBeforeDivider}/>
                {props.tabNode}
                <TabDivider isShow={props.showAfterDivider}/>
            </div>
        </Dropdown>
    );
});

interface TabNodeWrapperProps {
    prevTabNode: MutableRefObject<ReactElement>
    currentMouseOverNodeState: [any, Dispatch<SetStateAction<any>>]
}

const isSameTab = (tabNode1, tabNode2) => RvUtil.equalAndNotEmpty(tabNode1?.key, tabNode2?.key);
const isTabActive = (tabNode, currentTabKey) => RvUtil.equalAndNotEmpty(tabNode?.key, currentTabKey);

export default ({
                    prevTabNode,
                    currentMouseOverNodeState,
                }: TabNodeWrapperProps) => {
    const {
        sortedCachingNodes,
        currentTabKey,
    } = useContext<TabsContextType>(TabsContext);
    return (tabNode) => {
        const [currentMouseOverNode, setCurrentMouseOverNode] = currentMouseOverNodeState;
        const index = sortedCachingNodes.findIndex(cachingNode => cachingNode.name === tabNode.key);
        const cachingNode = sortedCachingNodes[index];
        let className = 'keep-alive-tab';
        const isFirst = index === 0;
        const isLast = index === sortedCachingNodes.length - 1;
        const isActive = isTabActive(tabNode, currentTabKey);
        if (index === -1) {
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
        }
        let showBeforeDivider = !isFirst;
        let showAfterDivider = isLast && !isFirst;
        if (isActive || isSameTab(currentMouseOverNode, tabNode)) {
            showBeforeDivider = false;
            showAfterDivider = false;
        }
        if (isTabActive(prevTabNode.current, currentTabKey) || isSameTab(currentMouseOverNode, prevTabNode.current)) {
            showBeforeDivider = false;
        }
        prevTabNode.current = tabNode;
        const onMouseEnter = () => {
            setCurrentMouseOverNode(tabNode);
        }
        const onMouseLeave = () => {
            setCurrentMouseOverNode(null);
        }
        // SortableTabNode的index属性是SortableElement的属性，不能删
        return (
            <SortableTabNode
                className={className}
                onMouseEnter={onMouseEnter}
                onMouseLeave={onMouseLeave}
                showBeforeDivider={showBeforeDivider}
                showAfterDivider={showAfterDivider}
                tabNode={tabNode}
                cachingNode={cachingNode}
                index={index}
            />
        )
    };
}
