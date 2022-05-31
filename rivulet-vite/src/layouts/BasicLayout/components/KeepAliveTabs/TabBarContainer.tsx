import {useCallback, useContext, useEffect, useRef, useState} from 'react';
import {SplitViewContainerType, SplitViewType, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {
    Active,
    closestCenter,
    CollisionDetection,
    DndContext,
    DragOverlay,
    getFirstCollision,
    MeasuringStrategy,
    PointerSensor,
    pointerWithin,
    useSensor,
    useSensors
} from '@dnd-kit/core';
import {arrayMove, horizontalListSortingStrategy, SortableContext} from '@dnd-kit/sortable';
import {createPortal} from 'react-dom';
import SortableTabBar from './SortableTabBar';
import {Modifier} from '@dnd-kit/core/dist/modifiers/types';
import {ClientRect} from '@dnd-kit/core/dist/types';

export default () => {
    const {
        keepAliveTabsElemRef,
        splitViewContainer,
        findNode,
        getSplitViewContainerCopy,
        resetSplitViewOfTabNodes,
        resetSplitViewContainer,
        removeSplitView,
        updateTabs
    } = useContext<TabsContextType>(TabsContext);
    const [activeTarget, setActiveTarget] = useState(null as unknown as Active);
    const sensors = useSensors(
        useSensor(PointerSensor, {
            activationConstraint: {
                distance: 5
            }
        })
    );
    const splitViewContainerCopyRef = useRef<SplitViewContainerType>();
    const findSplitView = splitViewId => splitViewContainer.splitViews.find(view => view.id === splitViewId);
    const findSplitViewCopy = splitView => splitViewContainerCopyRef.current?.splitViews.find(view => view.id === splitView.id) ?? {} as SplitViewType;
    const handleDragStart = (event) => {
        splitViewContainerCopyRef.current = getSplitViewContainerCopy();
        setActiveTarget(event.active);
    };
    const lastOverTarget = useRef<Active>();
    const lastOverSplitView = useRef<SplitViewType | undefined>();
    const handleDragCancel = () => {
        resetSplitViewContainer(splitViewContainerCopyRef.current ?? {} as SplitViewContainerType);
        lastOverSplitView.current = undefined;
        setActiveTarget(null as unknown as Active);
        updateTabs();
    };
    const activeTargetType = activeTarget?.data.current?.type;
    let activeTargetSplitView: SplitViewType | undefined;
    if (activeTargetType === 'tabBar') {
        activeTargetSplitView = findSplitView(activeTarget.id);
    } else if (activeTargetType === 'tabNode') {
        activeTargetSplitView = findNode(activeTarget.id)?.splitView;
    }
    useEffect(() => {
        lastOverSplitView.current = activeTargetSplitView;
        lastOverTarget.current = activeTarget;
    }, [activeTarget]);
    const handleDragOver = (event) => {
        const {over: overTarget} = event;
        if (!overTarget) return;
        lastOverTarget.current = overTarget;
        if (activeTargetType === 'tabBar' || !lastOverSplitView.current) return;
        const overTargetType = overTarget.data.current?.type;
        let overTargetSplitView;
        if (overTargetType === 'tabBar') {
            lastOverSplitView.current.tabNodes = lastOverSplitView.current.tabNodes.filter(node => node.name !== activeTarget.id);
            overTargetSplitView = findSplitView(overTarget.id);
        } else {
            overTargetSplitView = findNode(overTarget.id)?.splitView;
        }
        const overTargetSplitViewCopy = findSplitViewCopy(overTargetSplitView);
        const activeTargetSplitViewCopy = findSplitViewCopy(activeTargetSplitView);
        // 对应splitView不存在，直接退出
        if (!overTargetSplitView || !activeTargetSplitView) return;
        // 没有离开上一次的splitView，不需要处理
        if (overTargetSplitView.id === lastOverSplitView.current.id) return;
        // 将activeTarget从上一个splitView中移除
        lastOverSplitView.current.tabNodes = lastOverSplitView.current.tabNodes.filter(node => node.name !== activeTarget.id);
        lastOverSplitView.current = overTargetSplitView;
        const overIndex = overTargetSplitView.tabNodes.findIndex(node => node.name === overTarget.id);
        const activeTargetRect = activeTarget.rect.current;
        // activeTarget的左侧边界在overTarget的右侧边界的左侧减二分之一宽度之后，则认为在右侧
        const isRightSideOfOverTarget = activeTargetRect.translated
            && activeTargetRect.translated.left > overTarget.rect.right - overTarget.rect.width / 2;
        const indexDiff = isRightSideOfOverTarget ? 1 : 0;
        const newIndex = overIndex >= 0 ? overIndex + indexDiff : overTargetSplitView.tabNodes.length + 1;
        const overTargetTabsCopy = overTargetSplitViewCopy.tabNodes;
        const activeTargetTabsCopy = activeTargetSplitViewCopy.tabNodes;
        if (overTargetSplitView.id === activeTargetSplitView.id) {
            const tabNodesWithoutTarget = overTargetTabsCopy.filter(tab => tab.name !== activeTarget.id);
            overTargetSplitView.tabNodes = [
                ...tabNodesWithoutTarget.slice(0, newIndex),
                findNode(activeTarget?.id),
                ...tabNodesWithoutTarget.slice(newIndex, tabNodesWithoutTarget.length)
            ];
        } else {
            overTargetSplitView.tabNodes = [
                ...overTargetTabsCopy.slice(0, newIndex),
                findNode(activeTarget?.id),
                ...overTargetTabsCopy.slice(newIndex, overTargetTabsCopy.length)
            ];
            activeTargetSplitView.tabNodes = activeTargetTabsCopy.filter(tab => tab.name !== activeTarget.id);
        }
        updateTabs();
    };
    const handleDragEnd = (event) => {
        const {over} = event;
        const overTarget = over ?? lastOverTarget.current;
        const doHandle = () => {
            if (!overTarget || !activeTargetSplitView) return;
            if (activeTargetType === 'tabBar') {
                const overSplitViewIndex = splitViewContainer.splitViews.findIndex(view => view.id === overTarget.id);
                const activeSplitViewIndex = splitViewContainer.splitViews.findIndex(view => view.id === activeTarget.id);
                if (overSplitViewIndex === activeSplitViewIndex) return;
                splitViewContainer.splitViews = arrayMove(splitViewContainer.splitViews, activeSplitViewIndex, overSplitViewIndex);
            } else if (activeTargetType === 'tabNode') {
                const overTargetNode = findNode(overTarget.id);
                const activeTargetNode = findNode(activeTarget.id);
                if (!overTargetNode || !activeTargetNode || !lastOverSplitView.current) return;
                let overTargetSplitView = overTargetNode.splitView;
                if (overTarget.id === activeTarget.id) {
                    overTargetSplitView = lastOverSplitView.current;
                }
                if (activeTargetSplitView.tabNodes.length === 0) {
                    removeSplitView(activeTargetSplitView.id);
                }
                const activeTargetRect = activeTarget.rect.current.translated as ClientRect;
                const overTargetRect = overTargetNode.sortableAttr?.rect.current as ClientRect;
                const overNodeIndex = overTargetSplitView.tabNodes.findIndex(node => node.name === overTarget.id);
                const activeNodeIndex = overTargetSplitView.tabNodes.findIndex(node => node.name === activeTarget.id);
                if (activeTargetNode.splitView.id !== overTargetSplitView.id) {
                    // 如果是从其他splitView进入，并且结束时在最右侧的，不对换位置
                    if (activeNodeIndex === overTargetSplitView.tabNodes.length - 1
                        && activeTargetRect.left > overTargetRect.right - overTargetRect.width / 2) {
                        return;
                    }
                    // 在最左侧的，也不对换位置
                    if (activeNodeIndex === 0
                        && activeTargetRect.right < overTargetRect.left + overTargetRect.width / 2) {
                        return;
                    }
                }
                overTargetSplitView.tabNodes = arrayMove(overTargetSplitView.tabNodes, activeNodeIndex, overNodeIndex);
            }
        };
        doHandle();
        resetSplitViewOfTabNodes();
        setActiveTarget(null as unknown as Active);
        updateTabs();
    };
    let overlayElement: any = null;
    if (activeTarget !== null) {
        const type = activeTarget.data.current?.type;
        if (type === 'tabNode') {
            const activeNode = findNode(activeTarget.id);
            overlayElement = (
                <div className="keep-alive-tab-overlay">
                    {activeNode?.tabElement ?? null}
                </div>
            );
        } else if (type === 'tabBar') {
            const activeTabBar = splitViewContainer.splitViews.find(view => view.id === activeTarget.id);
            overlayElement = (
                <div className="keep-alive-tab-bar-overlay">
                    {activeTabBar?.tabBarElement ?? null}
                </div>
            );
        }
    }
    const collisionDetectionStrategy: CollisionDetection = useCallback(args => {
        const activeTargetType = activeTarget?.data.current?.type;
        if (activeTargetType === 'tabBar') {
            return closestCenter({
                ...args,
                droppableContainers: args.droppableContainers.filter(
                    container => container.data.current?.type === 'tabBar'
                )
            });
        } else if (activeTargetType === 'tabNode') {
            let intersections = pointerWithin(args);
            if (intersections.length === 0) {
                intersections = closestCenter(args);
            }
            let overTarget = getFirstCollision(intersections);
            if (!overTarget) return [];
            const overTargetType = overTarget?.data?.droppableContainer.data.current.type;
            tabBar: if (overTargetType === 'tabBar') {
                const overTargetSplitView = splitViewContainer.splitViews.find(view => view.id === overTarget?.id);
                if (!overTargetSplitView) break tabBar;
                const containers = args.droppableContainers.filter(
                    container => {
                        const type = container.data.current?.type;
                        return type === 'tabNode'
                            // && container.id !== activeTarget.id
                            && overTargetSplitView.tabNodes.some(node => node.name === container.id);
                    }
                );
                if (containers.length === 0) break tabBar;
                overTarget = closestCenter({
                    ...args,
                    droppableContainers: containers
                })[0];
            }
            if (overTarget.id === activeTarget.id) return [];
            return [overTarget];
        }
        return [];
    }, [activeTarget, splitViewContainer]);
    const dragOverlay = (
        <DragOverlay>
            {overlayElement}
        </DragOverlay>
    );
    const restrictToTabs: Modifier = (args) => {
        const {overlayNodeRect, transform} = args;
        const containerRect = keepAliveTabsElemRef.current?.getBoundingClientRect();
        if (!containerRect || !overlayNodeRect) {
            return transform;
        }
        if (overlayNodeRect.left + transform.x < containerRect.left) {
            transform.x = containerRect.left - overlayNodeRect.left;
        }
        if (overlayNodeRect.right + transform.x > containerRect.right) {
            transform.x = containerRect.right - overlayNodeRect.right;
        }
        if (overlayNodeRect.top + transform.y < containerRect.top) {
            transform.y = containerRect.top - overlayNodeRect.top;
        }
        // 向下拓展一个原始高度的区域，可以在此区域内拖动
        if (overlayNodeRect.bottom + transform.y > containerRect.bottom + overlayNodeRect.height) {
            transform.y = containerRect.bottom + overlayNodeRect.height - overlayNodeRect.bottom;
        }
        return transform;
    };
    const className = activeTarget !== null ? 'keep-alive-tab-bar-moving' : '';
    return (
        <DndContext
            sensors={sensors}
            collisionDetection={collisionDetectionStrategy}
            measuring={{
                droppable: {
                    strategy: MeasuringStrategy.Always
                }
            }}
            modifiers={[restrictToTabs]}
            onDragStart={handleDragStart}
            onDragOver={handleDragOver}
            onDragCancel={handleDragCancel}
            onDragEnd={handleDragEnd}
        >
            <SortableContext
                items={splitViewContainer.splitViews.map(view => view.id)}
                strategy={horizontalListSortingStrategy}
            >
                {splitViewContainer.splitViews.map(splitView => {
                    return <SortableTabBar className={className} key={splitView.id} splitView={splitView}/>;
                })}
            </SortableContext>
            {createPortal(dragOverlay, document.body)}
        </DndContext>
    );
};
