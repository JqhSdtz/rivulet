import {useHistory} from 'ice';
import {CachingNode, useAliveController} from 'react-activation';
import {CloseOutlined} from '@ant-design/icons';

import styles from './index.module.less';
import React, {useContext} from 'react';
import {MenuDataItem, RouteContext} from '@/layouts/BasicLayout';

interface KeepAliveTabProps {
    node: CachingNode;
}

const KeepAliveTab: React.FC<KeepAliveTabProps> = props => {
    const {node} = props;
    const history = useHistory();
    const {getCachingNodes, drop} = useAliveController();
    const cachingNodes: Array<CachingNode> = getCachingNodes();
    const closable = cachingNodes.length > 1;
    const {matchMenuKeys} = useContext(RouteContext);
    const defaultTabTitle = 'untitled';
    let tabTitle: string | undefined = defaultTabTitle;
    let isActive: boolean = false;
    if (node.matchMenus) {
        const [nodeMenuData] = node.matchMenus as Array<MenuDataItem>;
        tabTitle = nodeMenuData.name;
        const [matchMenuKey] = matchMenuKeys as Array<string>;
        isActive = matchMenuKey === nodeMenuData.key;
    }

    function dropTab(event) {
        event.stopPropagation();
        const currentName = node.name || '';
        if (isActive) {
            drop(currentName).then(() => {});
            const curIdx = cachingNodes.findIndex(
                routeNode => routeNode.name === currentName
            );
            history.push(curIdx > 0 ? cachingNodes[curIdx - 1].name || '' : '');
        } else {
            drop(currentName).then(() => {});
        }
    }

    return (
        <li
            className={isActive ? styles['active'] : ''}
            onClick={() => {
                history.push(node.name || '');
            }}
        >
            {tabTitle || defaultTabTitle}
            {closable && (
                <CloseOutlined
                    className={styles['close-btn']}
                    onClick={dropTab}
                />
            )}
        </li>
    );
};

export default KeepAliveTab;
