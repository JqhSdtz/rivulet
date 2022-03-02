import {useAliveController} from 'react-activation';

import React, {useContext} from 'react';
import {HeaderViewProps} from '@/layouts/BasicLayout/Header';
import {headerHeight, MenuDataItem, RouteContext} from '@/layouts/BasicLayout';
import {Tabs} from 'antd';
import {useHistory} from 'ice';

const {TabPane} = Tabs;

const KeepAliveTabs: React.FC<HeaderViewProps> = () => {
    const {getCachingNodes, drop} = useAliveController();
    const cachingNodes = getCachingNodes();
    const {matchMenuKeys} = useContext(RouteContext);
    const history = useHistory();
    const defaultTabTitle = 'untitled';
    const [matchMenuKey] = matchMenuKeys as Array<string>;
    const getTargetNode = targetKey => cachingNodes.find(node => {
        const [nodeMenuData] = node.matchMenus as Array<MenuDataItem>;
        return nodeMenuData?.key === targetKey;
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
    const tabClosable = cachingNodes.length > 1;
    return (
        <Tabs
            hideAdd
            type="editable-card"
            tabBarStyle={{height: headerHeight}}
            activeKey={matchMenuKey}
            onChange={onChange}
            onEdit={onEdit}
        >
            {cachingNodes.map((node, index) => {
                const [nodeMenuData] = node.matchMenus as Array<MenuDataItem>;
                const tabTitle = nodeMenuData?.name || defaultTabTitle;
                const tabKey = nodeMenuData?.key || index.toString();
                return (
                    <TabPane tab={tabTitle} key={tabKey} closable={tabClosable}/>
                )
            })}
        </Tabs>
    );
};

export default KeepAliveTabs;
