import {Button, Result} from 'antd';
import store from '@/store';

export default () => {
    const appDispatchers = store.useModelDispatchers('app');
    const onEnter = () => {
        appDispatchers.finishAppInit(null);
    }
    return (
        <Result
            status="success"
            title="应用初始化成功！"
            subTitle="初始用户拥有系统最高权限，请务必牢记初始用户的用户名和密码"
            extra={[
                <Button type="primary" onClick={onEnter}>
                    进入应用
                </Button>
            ]}
        />
    );
}
