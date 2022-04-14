import {Item, ItemParams, Menu} from 'react-contexify';
import {createPortal} from 'react-dom';
import {CachingNodeHandler} from './cachingNodeHandler';

export default (props: { menuId: string, cachingNodeHandler: CachingNodeHandler }) => {
    const handler = props.cachingNodeHandler;
    const closeTab = ({props}: ItemParams<typeof props>) => {
        const {cachingNode} = props;
        handler.removeCachingNode(cachingNode.name);
    }
    return createPortal((
        <Menu id={props.menuId}>
            <Item onClick={closeTab} hidden={handler.sortedCachingNodes.length <= 1}>
                关闭
            </Item>
            <Item>
                Item 2
            </Item>
            <Item disabled>Disabled</Item>
        </Menu>
    ), document.body);
}
