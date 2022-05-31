import {createElement, useEffect} from 'react';
import type {BasicLayoutProps} from './BasicLayout';
import ProLayout from './BasicLayout';
import type {FooterProps} from './components/Footer';
import DefaultFooter from './components/Footer';
import type {HeaderViewProps, HeaderViewProps as HeaderProps} from './components/Header';
import DefaultHeader from './components/Header';
import type {TopNavHeaderProps} from './components/TopNavHeader';
import TopNavHeader from './components/TopNavHeader';
import type {SettingDrawerProps, SettingDrawerState} from './components/SettingDrawer';
import SettingDrawer from './components/SettingDrawer';
import GridContent from './components/GridContent';
import type {PageContainerProps} from './components/PageContainer';
import PageContainer, {ProBreadcrumb, ProPageHeader} from './components/PageContainer';
import type {RouteContextType} from './contexts/RouteContext';
import RouteContext from './contexts/RouteContext';
import getMenuData from './utils/getMenuData';
import getPageTitle from './components/getPageTitle';
import PageLoading from './components/PageLoading';
import FooterToolbar from './components/FooterToolbar';
import WaterMark from './components/WaterMark';
import {asideMenuConfig, MenuConfigItem} from '@/menuConfig';
import KeepAliveTabs from '@/layouts/BasicLayout/components/KeepAliveTabs';
import {SiderMenuProps} from '@/layouts/BasicLayout/components/SiderMenu/SiderMenu';
import UserCenterMenu from '@/layouts/BasicLayout/customs/UserCenterMenu';
import {Link, Outlet, useLocation} from 'react-router-dom';

const loopMenuItem = (menus: any) =>
    menus.map(({icon, children, ...item}: any) => ({
        ...item,
        icon: createElement(icon),
        children: children && loopMenuItem(children)
    }));

const menuDataRender = async () => {
    const config: Function | any = asideMenuConfig;
    let menuItems = config;
    if (typeof asideMenuConfig === 'function') {
        menuItems = await config();
    }
    return loopMenuItem(menuItems);
};

const menuItemRender = (item: MenuConfigItem, defaultDom: any) => {
    if (!item.path) {
        return defaultDom;
    }
    let path = item.path;
    if (!item.isStartPage) {
        const separator = item.path.indexOf('?') > -1 ? '&' : '?';
        // 加时间戳和随机数以保证每次点击菜单都生成一个新的页面
        path += separator + '_timestamp=' + Date.now();
    }
    return <Link to={path}>{defaultDom}</Link>;
};

export default () => {
    useEffect(() => {
        window.onbeforeunload = () => '直接关闭可能会丢失部分您正在执行的操作，是否继续？';
    }, []);
    const location = useLocation();
    return (
        <ProLayout
            title="icejs & antd"
            style={{
                minHeight: '100vh'
            }}
            contentStyle={{
                margin: '0.5rem 1rem'
            }}
            headerHeight=""
            location={{
                pathname: location.pathname
            }}
            fixSiderbar
            fixedHeader
            menu={{
                autoClose: false,
                request: menuDataRender
            }}
            menuItemRender={menuItemRender}
            bottomButtonsRender={(props: SiderMenuProps) => <UserCenterMenu {...props} />}
            headerRender={(props: HeaderViewProps) => <KeepAliveTabs {...props} />}
            footerRender={() => (
                <DefaultFooter
                    links={[
                        {
                            key: 'icejs',
                            title: 'icejs',
                            href: 'https://github.com/ice-lab/icejs'
                        },
                        {
                            key: 'antd',
                            title: 'antd',
                            href: 'https://github.com/ant-design/ant-design'
                        }
                    ]}
                    copyright="by icejs & antd"
                />
            )}
        >
            <div style={{minHeight: '60vh'}}>
                <Outlet/>
            </div>
        </ProLayout>
    );
}

export type {ProSettings} from './configs/defaultSettings';

export type {MenuDataItem} from './typings';

export {
    RouteContext,
    PageLoading,
    GridContent,
    DefaultHeader,
    TopNavHeader,
    DefaultFooter,
    SettingDrawer,
    getPageTitle,
    getMenuData,
    PageContainer,
    FooterToolbar,
    WaterMark,
    ProPageHeader,
    ProBreadcrumb
};

export * from './components/KeepAliveTabs';

export type {
    FooterProps,
    PageContainerProps,
    TopNavHeaderProps,
    BasicLayoutProps,
    RouteContextType,
    HeaderProps,
    SettingDrawerProps,
    SettingDrawerState
};
