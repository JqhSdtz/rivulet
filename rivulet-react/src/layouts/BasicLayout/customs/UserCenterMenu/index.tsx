import {SiderMenuProps} from '@/layouts/BasicLayout/components/SiderMenu/SiderMenu';
import {Menu} from 'antd';
import {PoweroffOutlined as PowerOffOutlined} from '@ant-design/icons/lib/icons';
import {request, useAuth} from 'ice';
import {UserOutlined} from '@ant-design/icons';
import './index.less';
import RvModal from '@/components/Common/RvModal';
import store from '@/store';

export default (props: SiderMenuProps) => {
    const {prefixCls} = props;
    const [, setAuth] = useAuth();
    const userState = store.useModelState('user');

    function logout() {
        RvModal.confirm({
            title: '确认退出登录？',
            onOk: async () => {
                const result: Result = await request.post('/auth/logout');
                if (result.successful) {
                    setAuth({
                        hasLoggedIn: false,
                    });
                } else {
                    RvModal.error({
                        content: '退出登录失败！' + result.errorMessage,
                    });
                }
            },
        });
    }

    return (
        <Menu.SubMenu key="userCenterMenu" icon={<UserOutlined/>} title={userState.username}>
            <Menu.Item key="logout" onClick={logout}>
                <span
                    title="退出登录"
                    className={`${prefixCls}-menu-item`}
                >
                    <PowerOffOutlined/>
                    <span className={`${prefixCls}-menu-item-title`}>
                        退出登录
                    </span>
                </span>
            </Menu.Item>
        </Menu.SubMenu>
    )
}
