import {SmileOutlined} from '@ant-design/icons';

export interface MenuConfigItem {
    name: string,
    path: string,
    icon?: any,
    testPath: (path: string | undefined) => boolean
}

function testPath(this: MenuConfigItem, path) {
    // 需要判断例如 /test?v=1&t=0 是否属于路径为/test?v=1的菜单项
    if (!path || path.length < this.path.length) {
        return false;
    } else if (!path.startsWith(this.path)) {
        return false;
    } else if (path.length === this.path.length) {
        return true;
    } else {
        // 获取待判断路径去掉匹配的部分后剩下部分的第一个字符
        const ch = path[this.path.length];
        return ch === '?' || ch === '&' || ch === '/' || ch === '\\';
    }
}

const config: MenuConfigItem[] = [
    {
        name: '首页',
        path: '/',
        icon: SmileOutlined,
        testPath,
    },
    {
        name: '仪表盘',
        path: '/dashboard',
        icon: SmileOutlined,
        testPath,
    },
    {
        name: '数据模型',
        path: '/data_model',
        icon: SmileOutlined,
        testPath,
    },
];

for (let i = 0; i < 5; ++i) {
    config.push({
        name: '测试' + i,
        path: '/test?v=' + i,
        // path: '/test' + i,
        icon: SmileOutlined,
        testPath,
    });
}

function processConfig(targetConfig) {
    targetConfig.forEach(configItem => {
        configItem.key = configItem.path
    });
    return targetConfig;
}

const asideMenuConfig = () => new Promise(resolve => {
    setTimeout(() => resolve(processConfig(config)), 500);
});

export {asideMenuConfig};
