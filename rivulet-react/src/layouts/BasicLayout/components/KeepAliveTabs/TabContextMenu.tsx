import {Menu} from 'antd';
import {CachingNodeHandler} from './cachingNodeHandler';
import {CachingNodeType} from './CachingNode';
import {ReactElement} from 'react';

export default (props: {cachingNodeHandler: CachingNodeHandler, cachingNode: CachingNodeType}) => {
    const {
        cachingNodeHandler,
        cachingNode
    } = props;
    const {
        sortedCachingNodes,
        removeCachingNode
    } = cachingNodeHandler;
    const closeTab = () => {
        removeCachingNode(cachingNode.name);
    }
    const menuItems = [] as ReactElement[];
    if (sortedCachingNodes.length > 1) {
        menuItems.push(
            <Menu.Item key="close" onClick={closeTab}>
                关闭
            </Menu.Item>
        );
    }
    menuItems.push(
        <Menu.Item key="item2">
            Item 2
        </Menu.Item>
    );
    menuItems.push(
        <Menu.Item key="item3">
            Item 3
        </Menu.Item>
    );
    return (
        <Menu onContextMenu={(event) => event.preventDefault()}>
            {menuItems}
        </Menu>
    );
}
