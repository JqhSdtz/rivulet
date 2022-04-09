import {SiderMenuProps} from '@/layouts/BasicLayout/components/SiderMenu/SiderMenu';
import {Menu, Modal} from 'antd';
import {PoweroffOutlined as PowerOffOutlined} from '@ant-design/icons/lib/icons';
import {request, useAuth} from 'ice';
import {UserOutlined} from '@ant-design/icons';
import './index.less';

export default (props: SiderMenuProps) => {
    const {prefixCls} = props;
    const [, setAuth] = useAuth();

    function logout() {
        Modal.confirm({
            title: '确认退出登录？',
            onOk: async () => {
                const result: Result = await request.post('/auth/logout');
                if (result.successful) {
                    setAuth({
                        hasLoggedIn: false
                    });
                } else {
                    Modal.error({
                        content: '退出登录失败！' + result.errorMessage
                    });
                }
            }
        });
    }

    return (
        <Menu.SubMenu key="userCenterMenu" icon={<UserOutlined/>}>
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
