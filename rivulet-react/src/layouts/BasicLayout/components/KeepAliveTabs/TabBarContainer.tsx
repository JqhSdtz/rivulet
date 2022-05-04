import {useCallback, useContext, useRef, useState} from 'react';
import {SplitViewContainerType, SplitViewType, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import {
    Active,
    closestCenter,
    CollisionDetection,
    DndContext,
    DragOverlay,
    PointerSensor,
    useSensor,
    useSensors
} from '@dnd-kit/core';
import {horizontalListSortingStrategy, SortableContext} from '@dnd-kit/sortable';
import {createPortal} from 'react-dom';
import SortableTabBar from '@/layouts/BasicLayout/components/KeepAliveTabs/SortableTabBar';
import {Modifier} from '@dnd-kit/core/dist/modifiers/types';

export default () => {
    const {
        keepAliveTabsElemRef,
        splitViewContainer,
        findNode,
        getSplitViewContainerCopy,
        resetSplitViewContainer,
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
    const findSplitViewCopy = splitView => splitViewContainerCopyRef.current?.splitViews.find(view => view.id === splitView.id) ?? {} as SplitViewType;
    const handleDragStart = (event) => {
        splitViewContainerCopyRef.current = getSplitViewContainerCopy();
        setActiveTarget(event.active);
    };
    const handleDragCancel = () => {
        resetSplitViewContainer(splitViewContainerCopyRef.current ?? {} as SplitViewContainerType);
        setActiveTarget(null as unknown as Active);
        updateTabs();
    };
    const handleDragEnd = () => {
        setActiveTarget(null as unknown as Active);
    };
    const handleDragOver = (event) => {
        const {over: overTarget} = event;
        const activeTargetType = activeTarget.data.current?.type;
        if (!overTarget || activeTargetType === 'tabBar') {
            return;
        }
        const overTargetNode = findNode(overTarget.id);
        const overTargetSplitView = overTargetNode?.splitView;
        const overTargetSplitViewCopy = findSplitViewCopy(overTargetSplitView);
        const activeTargetNode = findNode(activeTarget.id);
        const activeTargetSplitView = activeTargetNode?.splitView;
        const activeTargetSplitViewCopy = findSplitViewCopy(activeTargetSplitView);
        if (!overTargetSplitView || !activeTargetSplitView
            || overTargetSplitView === activeTargetSplitView) {
            return;
        }
        const overIndex = overTargetSplitView.tabNodes.findIndex(node => node.name === overTarget.id);
        const activeTargetRect = activeTarget.rect.current;
        const isRightSideOfOverTarget = activeTargetRect.translated
            && activeTargetRect.translated.left > overTarget.rect.right;
        const indexDiff = isRightSideOfOverTarget ? 1 : 0;
        const newIndex = overIndex >= 0 ? overIndex + indexDiff : overTargetSplitView.tabNodes.length + 1;
        const overTargetTabsCopy = overTargetSplitViewCopy.tabNodes;
        const activeTargetTabsCopy = activeTargetSplitViewCopy.tabNodes;
        overTargetSplitView.tabNodes = [
            ...overTargetTabsCopy.slice(0, newIndex),
            activeTargetNode,
            ...overTargetTabsCopy.slice(newIndex, overTargetTabsCopy.length)
        ];
        activeTargetSplitView.tabNodes = activeTargetTabsCopy.filter(tab => tab.name !== activeTarget.id);
        updateTabs();
    };
    // const onSortEnd = (event) => {
    // if (oldIndex === newIndex) {
    //     return;
    // }
    // const prevTabKeySequence = tabKeySequence;
    // const curTabKeySequence = [] as string[];
    // prevTabKeySequence.forEach((tabKey, index) => {
    //     if (newIndex < oldIndex) {
    //         if (index === newIndex) {
    //             curTabKeySequence.push(prevTabKeySequence[oldIndex]);
    //         }
    //         if (index !== oldIndex) {
    //             curTabKeySequence.push(tabKey);
    //         }
    //     } else {
    //         if (index !== oldIndex) {
    //             curTabKeySequence.push(tabKey);
    //         }
    //         if (index === newIndex) {
    //             curTabKeySequence.push(prevTabKeySequence[oldIndex]);
    //         }
    //     }
    // });
    // setTabKeySequence(curTabKeySequence);
    // };
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
        const type = activeTarget?.data.current?.type;
        if (type === 'tabBar') {
            return closestCenter({
                ...args,
                droppableContainers: args.droppableContainers.filter(
                    container => container.data.current?.type === 'tabBar'
                )
            });
        } else if (type === 'tabNode') {
            return closestCenter({
                ...args,
                droppableContainers: args.droppableContainers.filter(
                    container => container.data.current?.type === 'tabNode'
                )
            });
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
    return (
        <DndContext
            sensors={sensors}
            collisionDetection={collisionDetectionStrategy}
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
                    return <SortableTabBar key={splitView.id} splitView={splitView}/>;
                })}
            </SortableContext>
            {createPortal(dragOverlay, document.body)}
        </DndContext>
    );
};
