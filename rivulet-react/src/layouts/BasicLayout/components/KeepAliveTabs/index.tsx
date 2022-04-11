import React, {useContext, useState} from 'react';
import {HeaderViewProps} from '@/layouts/BasicLayout/components/Header';
import {MenuDataItem, RouteContext} from '@/layouts/BasicLayout';
import {Tabs} from 'antd';
import {useHistory} from 'ice';
import {useAliveController} from 'react-activation';
import './index.less';
import RvUtil from '@/utils/rvUtil';
import {MenuConfigItem} from '@/layouts/BasicLayout/configs/menuConfig';

const {TabPane} = Tabs;

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
// 提示：KeepAlive的cachingNodes里的node的name是location的pathname+search
const KeepAliveTabs: React.FC<HeaderViewProps> = () => {
    const {getCachingNodes, drop} = useAliveController();
    const cachingNodes = getCachingNodes();
    const {matchMenuKeys, menuData} = useContext(RouteContext);
    const history = useHistory();
    const defaultTabTitle = '加载中...';
    const [matchMenuKey] = matchMenuKeys as Array<string>;
    cachingNodes.forEach(node => {
        menuData?.forEach((menu: MenuConfigItem) => {
            if (menu.testPath?.(node?.name)) {
                node.targetMenu = menu;
            }
        });
    });
    const getTargetNode = targetKey => cachingNodes.find(node => {
        const menu: MenuDataItem = node.targetMenu;
        return menu?.key === targetKey;
    });
    const onChange = (targetKey) => {
        const targetNode = getTargetNode(targetKey);
        history.push(targetNode?.name || '');
    }
    const onEdit = (targetKey, action) => {
        const targetNode = getTargetNode(targetKey);
        const isActive = targetKey === matchMenuKey;
        if (action === 'remove') {
            const currentName = targetNode?.name || '';
            if (isActive) {
                drop(currentName).then(() => {
                });
                const curIdx = cachingNodes.findIndex(
                    routeNode => routeNode.name === currentName
                );
                history.push(curIdx > 0 ? cachingNodes[curIdx - 1].name || '' : '');
            } else {
                drop(currentName).then(() => {
                });
            }
        }
    }

    const isTabActive = (tabNode) => RvUtil.equalAndNotEmpty(tabNode?.key, matchMenuKey);
    const isSameTab = (tabNode1, tabNode2) => RvUtil.equalAndNotEmpty(tabNode1?.key, tabNode2?.key);
    const tabClosable = cachingNodes.length > 1;
    let prevTabNode = null as any;
    const [currentMouseOverNode, setCurrentMouseOverNode] = useState(null as any);
    const tabRenderWrapper = (tabNode) => {
        const index = cachingNodes.findIndex(node => node.targetMenu?.key === tabNode.key);
        let className = 'keep-alive-tab';
        const isFirst = index === 0;
        const isLast = index === cachingNodes.length - 1;
        const isActive = isTabActive(tabNode);
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
        if (isTabActive(prevTabNode) || isSameTab(currentMouseOverNode, prevTabNode)) {
            showBeforeDivider = false;
        }
        prevTabNode = tabNode;

        const onMouseEnter = () => {
            setCurrentMouseOverNode(tabNode);
        }
        const onMouseLeave = () => {
            setCurrentMouseOverNode(null);
        }
        return (
            <div className={className} onMouseEnter={onMouseEnter} onMouseLeave={onMouseLeave}>
                <TabDivider isShow={showBeforeDivider}/>
                {tabNode}
                <TabDivider isShow={showAfterDivider}/>
            </div>
        );
    };
    const renderTabBar = (props, TabNavList) => {
        props.children = tabRenderWrapper;
        return <TabNavList {...props} />;
    }
    return (
        <Tabs
            type="editable-card"
            renderTabBar={renderTabBar}
            className="keep-alive-tabs"
            tabBarGutter={0}
            activeKey={matchMenuKey}
            onChange={onChange}
            onEdit={onEdit}
            animated={{inkBar: true, tabPane: false}}
        >
            {cachingNodes.map((node, index) => {
                const menu: MenuDataItem = node.targetMenu;
                const tabTitle = menu?.name || defaultTabTitle;
                const tabKey = menu?.key || index.toString();
                return (
                    <TabPane tab={tabTitle} key={tabKey} closable={tabClosable}/>
                )
            })}
        </Tabs>
    );
};

export default KeepAliveTabs;
