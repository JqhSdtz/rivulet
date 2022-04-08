import {SmileOutlined} from '@ant-design/icons';

const config = [
    {
        name: '首页',
        path: '/',
        icon: SmileOutlined
    },
    {
        name: '仪表盘',
        path: '/dashboard',
        icon: SmileOutlined
    },
    {
        name: '数据模型',
        path: '/data_model',
        icon: SmileOutlined
    }
];

const asideMenuConfig = () => new Promise(resolve => {
   setTimeout(() => resolve(config), 500);
});

export {asideMenuConfig};
