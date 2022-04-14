import RvUtil from '@/utils/rvUtil';
import {CachingNodeType} from './CachingNode';
import {Dispatch, ReactElement, SetStateAction} from 'react';
import {SortableElement} from 'react-sortable-hoc';
import {CachingNodeHandler} from './cachingNodeHandler';

import {Dropdown} from 'antd';
import TabContextMenu from '@/layouts/BasicLayout/components/KeepAliveTabs/TabContextMenu';

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
    cachingNodeHandler: CachingNodeHandler,
    tabNode: ReactElement,
    cachingNode: CachingNodeType
}) => {
    const tabContextMenu = (
        <TabContextMenu
            cachingNodeHandler={props.cachingNodeHandler}
            cachingNode={props.cachingNode}
        />
    );
    return (
        <Dropdown
            overlay={tabContextMenu}
            trigger={['contextMenu']}
            overlayStyle={{
                border: 'rgb(175, 175, 175) 1px solid'
            }}
            destroyPopupOnHide
        >
            <div className={props.className}
                 onMouseEnter={props.onMouseEnter}
                 onMouseLeave={props.onMouseLeave}
            >
                <TabDivider isShow={props.showBeforeDivider}/>
                {props.tabNode}
                <TabDivider isShow={props.showAfterDivider}/>
            </div>
        </Dropdown>
    );
});

interface TabNodeWrapperProps {
    cachingNodeHandler: CachingNodeHandler,
    prevTabNode: WithCurrent<ReactElement>,
    currentMouseOverNodeState: [any, Dispatch<SetStateAction<any>>]
}

const isSameTab = (tabNode1, tabNode2) => RvUtil.equalAndNotEmpty(tabNode1?.key, tabNode2?.key);
const isTabActive = (tabNode, currentPath) => RvUtil.equalAndNotEmpty(tabNode?.key, currentPath);

export default ({
                    cachingNodeHandler,
                    prevTabNode,
                    currentMouseOverNodeState
                }: TabNodeWrapperProps) => {
    const {
        sortedCachingNodes,
        currentPath
    } = cachingNodeHandler;
    return (tabNode) => {
        const [currentMouseOverNode, setCurrentMouseOverNode] = currentMouseOverNodeState;
        const index = sortedCachingNodes.findIndex(cachingNode => cachingNode.name === tabNode.key);
        const cachingNode = sortedCachingNodes[index];
        let className = 'keep-alive-tab';
        const isFirst = index === 0;
        const isLast = index === sortedCachingNodes.length - 1;
        const isActive = isTabActive(tabNode, currentPath);
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
        if (isTabActive(prevTabNode.current, currentPath) || isSameTab(currentMouseOverNode, prevTabNode.current)) {
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
                cachingNodeHandler={cachingNodeHandler}
                tabNode={tabNode}
                cachingNode={cachingNode}
                index={index}
            />
        )
    };
}
