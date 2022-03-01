import {createElement} from 'react';
import type {BasicLayoutProps} from './BasicLayout';
import ProLayout from './BasicLayout';
import type {FooterProps} from './Footer';
import DefaultFooter from './Footer';
import {Link} from 'ice';
import type {HeaderViewProps as HeaderProps} from './Header';
import DefaultHeader from './Header';
import type {TopNavHeaderProps} from './components/TopNavHeader';
import TopNavHeader from './components/TopNavHeader';
import type {
    SettingDrawerProps,
    SettingDrawerState
} from './components/SettingDrawer';
import SettingDrawer from './components/SettingDrawer';
import GridContent from './components/GridContent';
import type {PageContainerProps} from './components/PageContainer';
import PageContainer, {
    ProBreadcrumb,
    ProPageHeader
} from './components/PageContainer';
import type {RouteContextType} from './RouteContext';
import RouteContext from './RouteContext';
import getMenuData from './utils/getMenuData';
import getPageTitle from './getPageTitle';
import PageLoading from './components/PageLoading';
import FooterToolbar from './components/FooterToolbar';
import WaterMark from './components/WaterMark';
import {asideMenuConfig} from './configs/menuConfig';
import KeepAliveTabs from '@/layouts/BasicLayout/components/KeepAliveTabs';

const loopMenuItem = menus =>
    menus.map(({icon, children, ...item}) => ({
        ...item,
        icon: createElement(icon),
        children: children && loopMenuItem(children)
    }));

export const headerHeight = "3rem";

export default function BasicLayout({children, location}) {
    return (
        <ProLayout
            title="icejs & antd"
            style={{
                minHeight: '100vh'
            }}
            location={{
                pathname: location.pathname
            }}
            fixSiderbar
            fixedHeader
            headerHeight={headerHeight}
            menuDataRender={() => loopMenuItem(asideMenuConfig)}
            menuItemRender={(item, defaultDom) => {
                if (!item.path) {
                    return defaultDom;
                }
                return <Link to={item.path}>{defaultDom}</Link>;
            }}
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

const PageHeaderWrapper = PageContainer;

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
    PageHeaderWrapper,
    getMenuData,
    PageContainer,
    FooterToolbar,
    WaterMark,
    ProPageHeader,
    ProBreadcrumb
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
