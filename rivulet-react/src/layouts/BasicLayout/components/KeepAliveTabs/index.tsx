import React, {useContext, useState} from 'react';
import {HeaderViewProps} from '@/layouts/BasicLayout/components/Header';
import {MenuDataItem, RouteContext} from '@/layouts/BasicLayout';
import {Tabs} from 'antd';
import {useHistory, useLocation} from 'ice';
import {useAliveController} from 'react-activation';
import './index.less';
import RvUtil from '@/utils/rvUtil';
import {MenuConfigItem} from '@/layouts/BasicLayout/configs/menuConfig';
import MouseOver from '@/components/Common/MouseOver';
import {CloseCircleFilled, CloseOutlined} from '@ant-design/icons';

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
//       同时，TabNode的key和cachingNodes里的node的name相同
const KeepAliveTabs: React.FC<HeaderViewProps> = () => {
    const {getCachingNodes, drop} = useAliveController();
    const cachingNodes = getCachingNodes();
    const {menuData} = useContext(RouteContext);
    const {pathname, search} = useLocation();
    const history = useHistory();
    const defaultTabTitle = '加载中...';
    const currentPath = pathname + search;
    cachingNodes.forEach(node => {
        menuData?.forEach((menu: MenuConfigItem) => {
            if (menu.testPath(node?.name)) {
                node.targetMenu = menu;
            }
        });
    });
    const getTargetNode = targetKey => cachingNodes.find(node => node.name === targetKey);
    const onChange = (targetKey) => {
        history.push(targetKey || '');
    }
    const onEdit = (targetKey, action) => {
        const targetNode = getTargetNode(targetKey);
        const isActive = targetKey === currentPath;
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

    const isTabActive = (tabNode) => RvUtil.equalAndNotEmpty(tabNode?.key, currentPath);
    const isSameTab = (tabNode1, tabNode2) => RvUtil.equalAndNotEmpty(tabNode1?.key, tabNode2?.key);
    const tabClosable = cachingNodes.length > 1;
    let prevTabNode = null as any;
    const [currentMouseOverNode, setCurrentMouseOverNode] = useState(null as any);
    const tabRenderWrapper = (tabNode) => {
        const index = cachingNodes.findIndex(cachingNode => cachingNode.name === tabNode.key);
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
    const closeIcon = (
        <MouseOver
            className="close-icon"
            onMouseOverClassName="close-icon-mouse-over"
            normal={<CloseOutlined/>}
            onMouseOver={<CloseCircleFilled style={{fontSize: 15}}/>}
        />
    );
    // const closeIcon = <CloseCircleOutlined/>;
    return (
        <Tabs
            type="editable-card"
            renderTabBar={renderTabBar}
            className="keep-alive-tabs"
            tabBarGutter={0}
            activeKey={currentPath}
            onChange={onChange}
            onEdit={onEdit}
            animated={{inkBar: true, tabPane: false}}
        >
            {cachingNodes.map((node, index) => {
                const menu: MenuDataItem = node.targetMenu;
                const tabTitle = menu?.name || defaultTabTitle;
                const tabKey = node.name || index.toString();
                return (
                    <TabPane tab={tabTitle} key={tabKey} closable={tabClosable} closeIcon={closeIcon}/>
                )
            })}
        </Tabs>
    );
};

export default KeepAliveTabs;
