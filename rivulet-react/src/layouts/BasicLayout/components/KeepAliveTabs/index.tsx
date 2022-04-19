import React, {ReactElement, useContext, useRef, useState} from 'react';
import {HeaderViewProps} from '../Header';
import {Tabs} from 'antd';
import './index.less';
import MouseOver from '@/components/Common/MouseOver';
import {CloseCircleFilled, CloseOutlined} from '@ant-design/icons';
import TabNodeWrapper from './TabNodeWrapper';
import {SortableContainer} from 'react-sortable-hoc';
import {TabsContext, TabsContextType} from './TabsContextProvider';
import {MenuConfigItem} from '@/layouts/BasicLayout/configs/menuConfig';

const {TabPane} = Tabs;

const SortableTabs = SortableContainer((props) => {
    return (
        <Tabs {...props}>
            {props.children}
        </Tabs>
    );
});

// 提示：cachingNodes里的node的name是location的pathname+search
// 同时，TabNode的key和cachingNodes里的node的name相同
const KeepAliveTabs: React.FC<HeaderViewProps> = () => {
    const defaultTabTitle = '加载中...';
    const tabsContext = useContext<TabsContextType>(TabsContext);
    const {
        sortedCachingNodes,
        tabKeySequence,
        setTabKeySequence,
        currentTabKey,
        activeNode,
        removeNode
    } = tabsContext;
    const onEdit = (targetKey, action) => {
        if (action === 'remove') {
            removeNode(targetKey);
        }
    };
    const prevTabNode = useRef<ReactElement>(null as any);
    const currentMouseOverNodeState = useState(null as any);
    const renderWrapper = TabNodeWrapper({
        currentMouseOverNodeState,
        prevTabNode
    });
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
    const closeIcon = (
        <MouseOver
            className="close-icon"
            onMouseOverClassName="close-icon-mouse-over"
            normal={<CloseOutlined/>}
            onMouseOver={<CloseCircleFilled style={{fontSize: 15}}/>}
        />
    );
    const tabClosable = sortedCachingNodes.length > 1 || !sortedCachingNodes[0]?.targetMenu?.isStartPage;
    const sortableHelperRef = useRef<any>();
    const sortableHelper = <div className="sortable-tab-bar-helper" ref={sortableHelperRef}/>;
    const onSortEnd = ({oldIndex, newIndex}) => {
        if (oldIndex === newIndex) {
            return;
        }
        const prevTabKeySequence = tabKeySequence;
        const curTabKeySequence = [] as string[];
        prevTabKeySequence.forEach((tabKey, index) => {
            if (newIndex < oldIndex) {
                if (index === newIndex) {
                    curTabKeySequence.push(prevTabKeySequence[oldIndex]);
                }
                if (index !== oldIndex) {
                    curTabKeySequence.push(tabKey);
                }
            } else {
                if (index !== oldIndex) {
                    curTabKeySequence.push(tabKey);
                }
                if (index === newIndex) {
                    curTabKeySequence.push(prevTabKeySequence[oldIndex]);
                }
            }
        });
        setTabKeySequence(curTabKeySequence);
    };
    return (
        <SortableTabs
            type="editable-card"
            renderTabBar={renderTabBar}
            className="keep-alive-tabs"
            tabBarGutter={0}
            activeKey={currentTabKey}
            onEdit={onEdit}
            animated={{inkBar: true, tabPane: false}}
            axis="x"
            lockAxis="x"
            distance={5}
            tabBarExtraContent={sortableHelper}
            helperContainer={() => sortableHelperRef.current}
            onSortEnd={onSortEnd}
        >
            {sortedCachingNodes.map((node, index) => {
                const menu: MenuConfigItem | undefined = node.targetMenu;
                const tabTitle = menu?.name || defaultTabTitle;
                const tabKey = node.name || index.toString();
                const tabElem = (
                    <span>
                        {menu?.icon}
                        {tabTitle}
                    </span>
                );
                return (
                    <TabPane tab={tabElem} key={tabKey} closable={tabClosable} closeIcon={closeIcon}/>
                );
            })}
        </SortableTabs>
    );
};

export default KeepAliveTabs;

export {default as CachingNode} from './TabNodeProvider';
export * from './TabsContextProvider';
