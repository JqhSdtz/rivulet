import React, {useContext, useRef, useState} from 'react';
import {HeaderViewProps} from '@/layouts/BasicLayout/components/Header';
import {MenuDataItem, RouteContext} from '@/layouts/BasicLayout';
import {Tabs} from 'antd';
import {useHistory, useLocation} from 'ice';
import {useAliveController} from 'react-activation';
import './index.less';
import {MenuConfigItem} from '@/layouts/BasicLayout/configs/menuConfig';
import MouseOver from '@/components/Common/MouseOver';
import {CloseCircleFilled, CloseOutlined} from '@ant-design/icons';
import TabNodeWrapper from '@/layouts/BasicLayout/components/KeepAliveTabs/TabNodeWrapper';
import {SortableContainer} from 'react-sortable-hoc';
import {CachingNodeType} from '@/layouts/BasicLayout/components/KeepAliveTabs/CachingNode';
import {useCreation, useUpdate} from 'ahooks';

const {TabPane} = Tabs;

const SortableTabs = SortableContainer((props) => {
    return (
        <Tabs {...props}>
            {props.children}
        </Tabs>
    );
});

function sortCachingNodes(tabKeySequence, cachingNodes): CachingNodeType[] {
    const sortedCachingNodes: CachingNodeType[] = [];
    tabKeySequence.forEach(tabKey => {
        const cachingNode = cachingNodes.find(node => node.name === tabKey);
        if (cachingNode) {
            sortedCachingNodes.push(cachingNode);
        }
    });
    return sortedCachingNodes;
}

function fillCachingNodeWithMenuData(sortedCachingNodes, menuData) {
    sortedCachingNodes.forEach(node => {
        menuData?.forEach((menu: MenuConfigItem) => {
            if (menu.testPath(node?.name)) {
                node.targetMenu = menu;
            }
        });
    });
}

function synchronizeTabKeySequence(prevTabKeySequence, cachingNodes): string[] {
    let curTabKeySequence = prevTabKeySequence.filter(tabKey => cachingNodes.findIndex(node => node.name === tabKey) !== -1);
    if (cachingNodes.length > prevTabKeySequence.length) {
        const addedTabKey: [] = cachingNodes.slice(prevTabKeySequence.length).map(node => node.name);
        curTabKeySequence = curTabKeySequence.concat(addedTabKey);
    }
    return curTabKeySequence;
}

function getTargetCachingNode(targetKey, sortedCachingNodes) {
    return sortedCachingNodes.find(node => node.name === targetKey);
}

// 提示：cachingNodes里的node的name是location的pathname+search
// 同时，TabNode的key和cachingNodes里的node的name相同
const KeepAliveTabs: React.FC<HeaderViewProps> = () => {
    const {getCachingNodes, drop} = useAliveController();
    const cachingNodes = getCachingNodes();
    const {menuData} = useContext(RouteContext);
    const {pathname, search} = useLocation();
    const history = useHistory();
    const forceUpdate = useUpdate();
    const defaultTabTitle = '加载中...';
    const currentPath = pathname + search;
    const tabKeySequence = useCreation(() => ({current: [] as string[]}), []);
    // 将cachingNodes中的增加和删除反映到tabKeySequence中
    tabKeySequence.current = synchronizeTabKeySequence(tabKeySequence.current, cachingNodes);
    // 对cachingNodes进行排序
    const sortedCachingNodes = sortCachingNodes(tabKeySequence.current, cachingNodes);
    // 设置cachingNode对应的MenuItem
    fillCachingNodeWithMenuData(sortedCachingNodes, menuData);
    const onChange = (targetKey) => {
        history.push(targetKey || '');
    }
    const onEdit = (targetKey, action) => {
        const targetNode = getTargetCachingNode(targetKey, sortedCachingNodes);
        const isActive = targetKey === currentPath;
        if (action === 'remove') {
            const currentName = targetNode?.name || '';
            if (isActive) {
                drop(currentName).then(() => {
                });
                const curIdx = sortedCachingNodes.findIndex(
                    routeNode => routeNode.name === currentName
                );
                history.push(curIdx > 0 ? sortedCachingNodes[curIdx - 1].name || '' : '');
            } else {
                drop(currentName).then(() => {
                });
            }
        }
    }

    const prevTabNode = useCreation(() => ({current: null}), []);
    const currentMouseOverNodeState = useState(null as any);
    const renderWrapper = TabNodeWrapper({
        sortedCachingNodes,
        currentPath,
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
