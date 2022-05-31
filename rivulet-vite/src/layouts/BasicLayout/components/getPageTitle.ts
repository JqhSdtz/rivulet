import type {MenuDataItem} from '../typings';
import type {ProSettings} from '../configs/defaultSettings';
import {MenuConfigItem} from '@/menuConfig';

export const matchParamsPath = (
    pathname: string,
    search: string,
    breadcrumb?: Record<string, MenuConfigItem>,
    breadcrumbMap?: Map<string, MenuConfigItem>
): MenuDataItem => {
    // Internal logic use breadcrumbMap to ensure the order
    // 内部逻辑使用 breadcrumbMap 来确保查询顺序
    const path = pathname + search;
    if (breadcrumbMap) {
        return [...breadcrumbMap.values()].find(value => value.testPath?.(path) ?? false) as MenuConfigItem;
    }

    // External uses use breadcrumb
    // 外部用户使用 breadcrumb 参数
    if (breadcrumb) {
        return Object.values(breadcrumb).find(value => value.testPath?.(path) ?? false) as MenuConfigItem;
    }

    return {
        path: ''
    };
};

export type GetPageTitleProps = {
    pathname?: string;
    search?: string,
    breadcrumb?: Record<string, MenuDataItem>;
    breadcrumbMap?: Map<string, MenuDataItem>;
    menu?: ProSettings['menu'];
    title?: ProSettings['title'];
    pageName?: string;
    formatMessage?: (data: { id: any; defaultMessage?: string }) => string;
};

/**
 * 获取关于 pageTitle 的所有信息方便包装
 *
 * @param props
 * @param ignoreTitle
 */
const getPageTitleInfo = (
    props: GetPageTitleProps,
    ignoreTitle?: boolean
): {
    // 页面标题
    title: string;
    // locale 的 title
    id: string;
    // 页面标题不带默认的 title
    pageName: string;
} => {
    const {
        pathname = '/',
        search = '',
        breadcrumb,
        breadcrumbMap,
        formatMessage,
        title,
        menu = {
            locale: false
        }
    } = props;
    const pageTitle = ignoreTitle ? '' : title || '';
    const currRouterData = matchParamsPath(pathname, search, breadcrumb, breadcrumbMap);
    if (!currRouterData) {
        return {
            title: pageTitle,
            id: '',
            pageName: pageTitle
        };
    }
    let pageName = currRouterData.name;

    if (menu.locale !== false && currRouterData.locale && formatMessage) {
        pageName = formatMessage({
            id: currRouterData.locale || '',
            defaultMessage: currRouterData.name
        });
    }

    if (!pageName) {
        return {
            title: pageTitle,
            id: currRouterData.locale || '',
            pageName: pageTitle
        };
    }
    if (ignoreTitle || !title) {
        return {
            title: pageName,
            id: currRouterData.locale || '',
            pageName
        };
    }
    return {
        title: `${pageName} - ${title}`,
        id: currRouterData.locale || '',
        pageName
    };
};

export {getPageTitleInfo};

const getPageTitle = (props: GetPageTitleProps, ignoreTitle?: boolean) => {
    return getPageTitleInfo(props, ignoreTitle).title;
};

export default getPageTitle;
