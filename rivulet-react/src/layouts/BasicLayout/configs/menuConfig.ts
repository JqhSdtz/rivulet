import {SmileOutlined} from '@ant-design/icons';

export interface MenuConfigItem {
    name: string,
    path: string,
    icon?: any,
    testPath?: (path: string | undefined) => boolean
}

const config: MenuConfigItem[] = [
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

for (let i = 0; i < 5; ++i) {
    config.push({
        name: '测试' + i,
        path: '/test?v=' + i,
        // path: '/test' + i,
        icon: SmileOutlined
    });
}

function processConfig(targetConfig) {
    targetConfig.forEach(configItem => {
        configItem.key = configItem.path
        configItem.testPath = function (path) {
            // return (path ?? '').startWith(this.path);
            return path === this.path;
        }
    });
    return targetConfig;
}

const asideMenuConfig = () => new Promise(resolve => {
    setTimeout(() => resolve(processConfig(config)), 500);
});

export {asideMenuConfig};
