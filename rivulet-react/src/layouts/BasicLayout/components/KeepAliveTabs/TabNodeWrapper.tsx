import RvUtil from '@/utils/rvUtil';
import {CachingNodeType} from '@/layouts/BasicLayout/components/KeepAliveTabs/CachingNode';
import {Dispatch, SetStateAction} from 'react';
import {SortableElement} from 'react-sortable-hoc';

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

const SortableTabNode = SortableElement((props) => (
    <div className={props.className} onMouseEnter={props.onMouseEnter} onMouseLeave={props.onMouseLeave}>
        <TabDivider isShow={props.showBeforeDivider}/>
        {props.tabNode}
        <TabDivider isShow={props.showAfterDivider}/>
    </div>
));

interface TabNodeWrapperProps {
    sortedCachingNodes: CachingNodeType[],
    currentPath: string,
    prevTabNode: WithCurrent,
    currentMouseOverNodeState: [any, Dispatch<SetStateAction<any>>],
}

const isSameTab = (tabNode1, tabNode2) => RvUtil.equalAndNotEmpty(tabNode1?.key, tabNode2?.key);
const isTabActive = (tabNode, currentPath) => RvUtil.equalAndNotEmpty(tabNode?.key, currentPath);

export default ({
                    sortedCachingNodes,
                    currentPath,
                    prevTabNode,
                    currentMouseOverNodeState
                }: TabNodeWrapperProps) => {
    return (tabNode) => {
        const [currentMouseOverNode, setCurrentMouseOverNode] = currentMouseOverNodeState;
        const index = sortedCachingNodes.findIndex(cachingNode => cachingNode.name === tabNode.key);
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
        return (
            <SortableTabNode
                className={className}
                onMouseEnter={onMouseEnter}
                onMouseLeave={onMouseLeave}
                showBeforeDivider={showBeforeDivider}
                showAfterDivider={showAfterDivider}
                tabNode={tabNode}
                index={index}
            />
        )
    };
}
