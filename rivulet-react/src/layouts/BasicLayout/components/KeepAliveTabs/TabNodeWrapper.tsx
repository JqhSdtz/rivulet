import RvUtil from '@/utils/rvUtil';
import {TabNodeType} from './TabNodeProvider';
import {Dispatch, MouseEvent, MutableRefObject, ReactElement, SetStateAction, useContext, useState} from 'react';
import {CSS} from '@dnd-kit/utilities';
import {TabsContext, TabsContextType} from './TabsContextProvider';
import TabContextMenu from './TabContextMenu';
import {Dropdown} from 'antd';
import {useSortable} from '@dnd-kit/sortable';

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
    props.tabNode.tabElement = props.tabNodeElem;
    const sortableProps = {
        id: props.tabNode.name ?? '',
        data: {
            type: 'tabNode'
        }
    };
    const {
        attributes,
        listeners,
        node,
        setNodeRef,
        transform,
        isDragging
    } = useSortable(sortableProps);
    const [contextMenuVisible, setContextMenuVisible] = useState(false);
    const onContextMenu = (event: MouseEvent) => {
        event.preventDefault();
        event.stopPropagation();
        setContextMenuVisible(true);
    };
    const tabContextMenu = (
        <TabContextMenu
            tabNode={props.tabNode}
            tabElemRef={node}
            setContextMenuVisible={setContextMenuVisible}
        />
    );
    // 没有找到antd的dropdown组件禁用关闭时的过渡效果的方法，应该是通过js控制的，
    // 所以采用直接添加class，直接设置display为none的方式实现立即关闭右键菜单
    let overlayClassName = 'keep-alive-tab-context-menu';
    if (!contextMenuVisible) overlayClassName += ' tab-context-menu-hidden';
    const sortableClassName = isDragging ? ' keep-alive-tab-dragged' : '';
    const sortableStyle = {
        transform: CSS.Transform.toString(transform),
        transition: 'transform 0.3s cubic-bezier(0.645, 0.045, 0.355, 1)'
    };

    return (
        <Dropdown
            overlay={tabContextMenu}
            trigger={['contextMenu']}
            overlayClassName={overlayClassName}
            visible={contextMenuVisible}
            destroyPopupOnHide
        >
            <div className={props.className + sortableClassName}
                 key={props.tabNode.name}
                 ref={setNodeRef}
                 style={sortableStyle}
                 {...attributes}
                 {...listeners}
                 onMouseEnter={props.onMouseEnter}
                 onMouseLeave={props.onMouseLeave}
                 onContextMenu={onContextMenu}
            >
                <TabDivider isShow={props.showBeforeDivider}/>
                {props.tabNodeElem}
                <TabDivider isShow={props.showAfterDivider}/>
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
