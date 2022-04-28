import {stripQueryStringAndHashFromPath} from '../transformRoute/transformRoute';

/**
 * 获取打平的 menuData
 * 以 path 为 key
 * @param menuData
 */
export const getFlatMenus = (menuData = []) => {
    let menus = {};
    menuData.forEach((item) => {
        if (!item || !item.key) {
            return;
        }
        menus[stripQueryStringAndHashFromPath(item.path || item.key || '/')] = {
            ...item
        };
        menus[item.key || item.path || '/'] = {...item};
        if (item.routes) {
            menus = {...menus, ...getFlatMenus(item.routes)};
        }
    });
    return menus;
};
export default getFlatMenus;
