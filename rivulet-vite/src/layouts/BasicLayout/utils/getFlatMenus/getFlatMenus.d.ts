import {MenuDataItem} from '../types';

/**
 * 获取打平的 menuData
 * 以 path 为 key
 * @param menuData
 */
export declare const getFlatMenus: (menuData?: MenuDataItem[]) => {
    [key: string]: MenuDataItem;
};
export default getFlatMenus;
