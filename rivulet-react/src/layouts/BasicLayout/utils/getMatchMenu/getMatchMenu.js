import getFlatMenu from '../getFlatMenus/getFlatMenus';

export const getMenuMatches = (flatMenus = [], path) => flatMenus
    .filter((item) => {
        return item.testPath?.(path) ?? false;
    })
    .map((item) => item.path)
    .sort((a, b) => {
        // 如果完全匹配放到最后面
        if (a === path) {
            return 10;
        }
        if (b === path) {
            return -10;
        }
        return a.substr(1).split('/').length - b.substr(1).split('/').length;
    });
/**
 * 获取当前的选中菜单列表
 * @param pathname
 * @param menuData
 * @returns FlatArray<unknown[][], 1>[]
 */
export const getMatchMenu = (pathname, menuData,
                             /**
                              * 要不要展示全部的 key
                              */
                             fullKeys) => {
    const flatMenus = getFlatMenu(menuData);
    let menuPathKeys = getMenuMatches(Object.values(flatMenus), pathname || '/');
    if (!menuPathKeys || menuPathKeys.length < 1) {
        return [];
    }
    if (!fullKeys) {
        menuPathKeys = [menuPathKeys[menuPathKeys.length - 1]];
    }
    return menuPathKeys
        .map((menuPathKey) => {
            const menuItem = flatMenus[menuPathKey] || {
                pro_layout_parentKeys: '',
                key: ''
            };
            // 去重
            const map = new Map();
            const parentItems = (menuItem.pro_layout_parentKeys || [])
                .map((key) => {
                    if (map.has(key)) {
                        return null;
                    }
                    map.set(key, true);
                    return flatMenus[key];
                })
                .filter((item) => item);
            if (menuItem.key) {
                parentItems.push(menuItem);
            }
            return parentItems;
        })
        .flat(1);
};
export default getMatchMenu;
