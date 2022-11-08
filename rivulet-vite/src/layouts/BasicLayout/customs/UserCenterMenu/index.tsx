import {SiderMenuProps} from '@/layouts/BasicLayout/components/SiderMenu/SiderMenu';
import {Menu} from 'antd';
import {PoweroffOutlined as PowerOffOutlined} from '@ant-design/icons/lib/icons';
import {UserOutlined} from '@ant-design/icons';
import './index.less';
import RvModal from '@/components/common/RvModal';
import store from '@/store';
import axios from 'axios';
import {Result} from '@/types/result';
import RvRequest from '@/utils/rvRequest';

export default (props: SiderMenuProps) => {
    const {prefixCls} = props;
    const authDispatchers = store.useModelDispatchers('auth');
    const userState = store.useModelState('user');

    function logout() {
        RvModal.confirm({
            title: '确认退出登录？',
            onOk: async () => {
                const result = await RvRequest.do(() => axios.post('/auth/logout'));
                if (result.successful) {
                    authDispatchers.setState({
                        hasLoggedIn: false
                    });
                } else {
                    RvModal.error({
                        content: '退出登录失败！' + result.errorMessage
                    });
                }
            }
        });
    }

    const items = [
        {
            key: 'userCenterMenu',
            label: userState.username,
            icon: <UserOutlined/>,
            children: [
                {
                    key: 'logout',
                    className: `${prefixCls}-menu-item`,
                    onClick: logout,
                    label: (
                        <span
                            title="退出登录"
                            className={`${prefixCls}-menu-item`}
                        >
                            <PowerOffOutlined/>
                            <span className={`${prefixCls}-menu-item-title`}>
                                退出登录
                            </span>
                        </span>
                    )
                }
            ]
        }
    ];

    return (
        <Menu theme={props.theme} mode="vertical" selectable={false} items={items}/>
    );
}
