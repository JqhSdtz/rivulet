import {CachingNodeType} from './CachingNode';
import {useHistory} from 'ice';
import {useAliveController} from 'react-activation';
import {useContext} from 'react';
import {RouteContext} from '@/layouts/BasicLayout';
import {useLocation} from '../../../../../.ice/index';
import {MenuConfigItem} from '@/layouts/BasicLayout/configs/menuConfig';
import {useCreation} from 'ahooks';

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

export interface CachingNodeHandler {
    sortedCachingNodes: CachingNodeType[],
    tabKeySequence: WithCurrent<string[]>,
    currentPath: string,
    activeCachingNode: (targetKey: string | undefined) => void,
    removeCachingNode: (targetKey: string | undefined) => void
}

export function useCachingNodeHandler(): CachingNodeHandler {
    const {getCachingNodes, drop} = useAliveController();
    const cachingNodes = getCachingNodes();
    const {menuData} = useContext(RouteContext);
    const {pathname, search} = useLocation();
    const history = useHistory();
    const currentPath = pathname + search;
    const tabKeySequence = useCreation(() => ({current: [] as string[]}), []);
    // 将cachingNodes中的增加和删除反映到tabKeySequence中
    tabKeySequence.current = synchronizeTabKeySequence(tabKeySequence.current, cachingNodes);
    // 对cachingNodes进行排序
    const sortedCachingNodes = sortCachingNodes(tabKeySequence.current, cachingNodes);
    // 设置cachingNode对应的MenuItem
    fillCachingNodeWithMenuData(sortedCachingNodes, menuData);
    const activeCachingNode = (targetKey) => {
        if (!targetKey) return;
        history.push(targetKey || '');
    }
    const removeCachingNode = (targetKey) => {
        if (!targetKey) return;
        const targetNode = getTargetCachingNode(targetKey, sortedCachingNodes);
        const isActive = targetKey === currentPath;
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
    return {
        sortedCachingNodes,
        tabKeySequence,
        currentPath,
        activeCachingNode,
        removeCachingNode
    }
}
