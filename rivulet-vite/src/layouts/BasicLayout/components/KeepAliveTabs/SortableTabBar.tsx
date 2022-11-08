import {SplitViewType, TabsContext, TabsContextType} from '@/layouts/BasicLayout';
import MouseOver from '@/components/common/MouseOver';
import {CloseCircleFilled, CloseOutlined} from '@ant-design/icons';
import {CSSProperties, ReactElement, useContext, useRef, useState} from 'react';
import {MenuConfigItem} from '@/menuConfig';
import {horizontalListSortingStrategy, SortableContext, useSortable} from '@dnd-kit/sortable';
import {CSS} from '@dnd-kit/utilities';
import {Tabs} from 'antd';
import {Handle} from '@/layouts/BasicLayout/components/KeepAliveTabs/components';
import {useCreation} from 'ahooks';
import TabNodeWrapper from '@/layouts/BasicLayout/components/KeepAliveTabs/TabNodeWrapper';

type SortableTabsProps = {
    className: string;
    splitView: SplitViewType;
};

const TabBar = (props) => {
    const {
        activeNode,
        removeNode
    } = useContext<TabsContextType>(TabsContext);
    const onEdit = (targetKey, action) => {
        if (action === 'remove') {
            removeNode(targetKey);
        }
    };
    const prevTabNode = useRef<ReactElement>(null as any);
    const currentMouseOverNodeState = useState(null as any);
    const renderWrapper = useCreation(() => (
        TabNodeWrapper({
            currentMouseOverNodeState,
            prevTabNode
        })
    ), []);
    const renderTabBar = (props, TabNavList) => {
        props.children = renderWrapper;
        props.onTabClick = (targetKey, event) => {
            // 补丁，解决antd的tabs组件直接阻断点击事件冒泡的问题，重新发放一个tabClick事件
            const eventInitDict: CustomEventInit = {
                bubbles: true,
                cancelable: true,
                detail: {
                    tabKey: targetKey
                }
            };
            const customEvent = new CustomEvent('tabClick', eventInitDict);
            event.target.dispatchEvent(customEvent);
            activeNode(targetKey);
        };
        return <TabNavList {...props} />;
    };
    const className = 'keep-alive-tab-bar' + (props.className ? (' ' + props.className) : '');
    const splitView = props.splitView as SplitViewType;
    const activeTabKey = splitView.tabNodes.find(node => node.isActive)?.name;
    // calc(100% - 12px)是因为左侧的handleButton的宽度是12px
    return (
        <Tabs type="editable-card"
              renderTabBar={renderTabBar}
              className={className}
              style={{width: 'calc(100% - 12px)'}}
              tabBarGutter={0}
              activeKey={activeTabKey}
              onEdit={onEdit}
              animated={{inkBar: true, tabPane: false}}
              hideAdd
              items={props.items}
        />
    );
};

export default (props: SortableTabsProps) => {
    const defaultTabTitle = '加载中...';
    const {splitView} = props;
    const closeIcon = (
        <MouseOver
            className="close-icon"
            onMouseOverClassName="close-icon-mouse-over"
            normal={<CloseOutlined/>}
            onMouseOver={<CloseCircleFilled style={{fontSize: 15}}/>}
        />
    );
    const {
        splitViewContainer
    } = useContext<TabsContextType>(TabsContext);
    const tabNodes = splitView.tabNodes.map((node, index) => {
        const menu: MenuConfigItem | undefined = node.targetMenu;
        const tabTitle = menu?.name || defaultTabTitle;
        const tabKey = node.name || index.toString();
        const tabElem = (
            <span>
                {menu?.icon}
                {tabTitle}
            </span>
        );
        return {
            label: tabElem,
            key: tabKey,
            closable: true,
            closeIcon
        };
    });
    const splitNum = splitViewContainer.splitViews.length;
    const sortableProps = {
        id: splitView.id,
        data: {
            type: 'tabBar'
        }
    };
    const {
        isDragging,
        setNodeRef,
        attributes,
        listeners,
        transform
    } = useSortable(sortableProps);
    const sortableStyle = {
        transform: CSS.Transform.toString(transform),
        transition: 'transform 0.3s cubic-bezier(0.645, 0.045, 0.355, 1)'
    };
    const style: CSSProperties = {
        ...sortableStyle,
        width: `${100 / splitNum}%`,
        display: 'inline-flex'
    };
    const splitViewIndex = splitViewContainer.splitViews.findIndex(view => view.id == splitView.id);
    splitView.tabBarElement = (
        <div style={{display: 'inline-flex', width: '100%'}}>
            {splitViewIndex > 0 && <Handle {...attributes} {...listeners}/>}
            <TabBar {...props} items={tabNodes}/>
        </div>
    );
    return (
        <div ref={setNodeRef}
             style={style}
             className={isDragging ? 'keep-alive-tab-bar-dragged' : ''}
        >
            <SortableContext
                items={splitView.tabNodes.map(node => node.name ?? '')}
                strategy={horizontalListSortingStrategy}
            >
                {splitView.tabBarElement}
            </SortableContext>
        </div>
    );
};
