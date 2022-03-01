import {useAliveController} from 'react-activation';

import React, {useContext} from 'react';
import {HeaderViewProps} from '@/layouts/BasicLayout/Header';
import {headerHeight, MenuDataItem, RouteContext} from '@/layouts/BasicLayout';
import {Tabs} from 'antd';

const {TabPane} = Tabs;

const KeepAliveTabs: React.FC<HeaderViewProps> = () => {
    const {getCachingNodes} = useAliveController();
    const cachingNodes = getCachingNodes();
    const {matchMenuKeys} = useContext(RouteContext);
    const defaultTabTitle = 'untitled';
    const [matchMenuKey] = matchMenuKeys as Array<string>;
    return (
        <Tabs
            hideAdd
            activeKey={matchMenuKey}
            type="editable-card"
            tabBarStyle={{height: headerHeight}}
        >
            {cachingNodes.map((node, index) => {
                const [nodeMenuData] = node.matchMenus as Array<MenuDataItem>;
                const tabTitle = nodeMenuData?.name || defaultTabTitle;
                const tabKey = nodeMenuData?.key || index.toString();
                return (
                    <TabPane tab={tabTitle} key={tabKey}/>
                )
            })}
        </Tabs>
    );
};

export default KeepAliveTabs;
