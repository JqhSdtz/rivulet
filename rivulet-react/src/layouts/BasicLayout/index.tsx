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
import KeepAliveTab from '@/layouts/BasicLayout/components/KeepAliveTabs/KeepAliveTab';
import {SiderMenuProps} from '@/layouts/BasicLayout/components/SiderMenu/SiderMenu';
import UserCenterMenu from '@/layouts/BasicLayout/customs/UserCenterMenu';
import {Menu} from 'antd';

const loopMenuItem = menus =>
    menus.map(({icon, children, ...item}) => ({
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
}

export default function BasicLayout({children, location}) {
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
            menu={{request: menuDataRender}}
            menuItemRender={(item, defaultDom) => {
                if (!item.path) {
                    return defaultDom;
                }
                return <Link to={item.path}>{defaultDom}</Link>;
            }}
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
    KeepAliveTab
};

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
