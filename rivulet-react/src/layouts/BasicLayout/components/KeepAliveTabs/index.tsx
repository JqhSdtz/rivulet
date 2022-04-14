import React, {useRef, useState} from 'react';
import {HeaderViewProps} from '../Header';
import {MenuDataItem} from '../..';
import {Tabs} from 'antd';
import './index.less';
import MouseOver from '@/components/Common/MouseOver';
import {CloseCircleFilled, CloseOutlined} from '@ant-design/icons';
import TabNodeWrapper from './TabNodeWrapper';
import {SortableContainer} from 'react-sortable-hoc';
import {useCreation, useUpdate} from 'ahooks';
import {useCachingNodeHandler} from './cachingNodeHandler';

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
    const forceUpdate = useUpdate();
    const defaultTabTitle = '加载中...';
    const cachingNodeHandler = useCachingNodeHandler();
    const {
        sortedCachingNodes,
        tabKeySequence,
        currentPath,
        activeCachingNode,
        removeCachingNode
    } = cachingNodeHandler;
    const onChange = (targetKey) => {
        activeCachingNode(targetKey);
    }
    const onEdit = (targetKey, action) => {
        if (action === 'remove') {
            removeCachingNode(targetKey);
        }
    }
    const prevTabNode = useCreation(() => ({current: null as any}), []);
    const currentMouseOverNodeState = useState(null as any);
    const renderWrapper = TabNodeWrapper({
        cachingNodeHandler,
        currentMouseOverNodeState,
        prevTabNode
    });
    const renderTabBar = (props, TabNavList) => {
        props.children = renderWrapper;
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
    const tabClosable = sortedCachingNodes.length > 1;
    const sortableHelperRef = useRef<any>();
    const sortableHelper = <div className="sortable-tab-bar-helper" ref={sortableHelperRef}/>;
    const onSortEnd = ({oldIndex, newIndex}) => {
        if (oldIndex === newIndex) {
            return;
        }
        const prevTabKeySequence = tabKeySequence.current;
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
        tabKeySequence.current = curTabKeySequence;
        forceUpdate();
    }
    return (
        <SortableTabs
            type="editable-card"
            renderTabBar={renderTabBar}
            className="keep-alive-tabs"
            tabBarGutter={0}
            activeKey={currentPath}
            onChange={onChange}
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
                const menu: MenuDataItem = node.targetMenu;
                const tabTitle = menu?.name || defaultTabTitle;
                const tabKey = node.name || index.toString();
                return (
                    <TabPane tab={tabTitle} key={tabKey} closable={tabClosable} closeIcon={closeIcon}/>
                )
            })}
        </SortableTabs>
    );
};

export default KeepAliveTabs;

export {default as CachingNode} from './CachingNode';
