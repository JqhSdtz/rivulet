import {createElement} from 'react';
import type {BasicLayoutProps} from './BasicLayout';
import ProLayout from './BasicLayout';
import type {FooterProps} from './components/Footer';
import DefaultFooter from './components/Footer';
import {Link} from 'ice';
import type {HeaderViewProps as HeaderProps} from './components/Header';
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
import {asideMenuConfig} from './configs/menuConfig';
import KeepAliveTabs from '@/layouts/BasicLayout/components/KeepAliveTabs';
import {SiderMenuProps} from '@/layouts/BasicLayout/components/SiderMenu/SiderMenu';
import UserCenterMenu from '@/layouts/BasicLayout/customs/UserCenterMenu';
import {Menu} from 'antd';

const loopMenuItem = menus =>
    menus.map(({icon, children, ...item}) => ({
        ...item,
        icon: createElement(icon),
        children: children && loopMenuItem(children),
    }));

const menuDataRender = async () => {
    const config: Function | any = asideMenuConfig;
    let menuItems = config;
    if (typeof asideMenuConfig === 'function') {
        menuItems = await config();
    }
    return loopMenuItem(menuItems);
}

const menuItemRender = (item, defaultDom) => {
    if (!item.path) {
        return defaultDom;
    }
    const separator = item.path.indexOf('?') > -1 ? '&' : '?';
    // 加时间戳以实现每次点击菜单都生成一个新的页面，同时还能防止点击过快
    // 比如时间出除以500，就可以保证两次打开页面的间隔大于500毫秒
    const path = item.path + separator + '_timestamp=' + Math.floor(Date.now() / 500);
    return <Link to={path}>{defaultDom}</Link>;
};

export default function BasicLayout({children, location}) {
    return (
        <ProLayout
            title="icejs & antd"
            style={{
                minHeight: '100vh',
            }}
            contentStyle={{
                margin: '0.5rem 1rem',
            }}
            headerHeight=""
            location={{
                pathname: location.pathname,
            }}
            fixSiderbar
            fixedHeader
            menu={{request: menuDataRender}}
            menuItemRender={menuItemRender}
            bottomButtonsRender={(props: SiderMenuProps) => (
                <Menu theme={props.theme} mode="vertical" selectable={false}>
                    <UserCenterMenu {...props} />
                </Menu>
            )}
            headerRender={props => <KeepAliveTabs {...props} />}
            footerRender={() => (
                <DefaultFooter
                    links={[
                        {
                            key: 'icejs',
                            title: 'icejs',
                            href: 'https://github.com/ice-lab/icejs',
                        },
                        {
                            key: 'antd',
                            title: 'antd',
                            href: 'https://github.com/ant-design/ant-design',
                        },
                    ]}
                    copyright="by icejs & antd"
                />
            )}
        >
            <div style={{minHeight: '60vh'}}>{children}</div>
        </ProLayout>
    );
}

export type {ProSettings} from './configs/defaultSettings';

export type {MenuDataItem} from './typings';

export {
    BasicLayout,
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
    ProBreadcrumb,
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
    SettingDrawerState,
};
