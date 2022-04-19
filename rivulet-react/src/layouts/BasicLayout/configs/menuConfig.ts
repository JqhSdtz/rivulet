import {SmileOutlined} from '@ant-design/icons';
import {MenuDataItem} from '@/layouts/BasicLayout';

export interface MenuConfigItem extends MenuDataItem{
    name: string;
    path: string;
    icon?: any;
    isStartPage?: boolean;
    testPath: (path: string | undefined) => boolean;
    children?: MenuConfigItem[]
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

export const defaultStartPage = '/';

const config: MenuConfigItem[] = [
    {
        name: '首页',
        path: '/',
        icon: SmileOutlined,
        testPath,
        children: [
            {
                name: '测试子菜单0',
                path: '/test?v=100',
                icon: SmileOutlined,
                testPath
            },
            {
                name: '测试子菜单1',
                path: '/test?v=101',
                icon: SmileOutlined,
                testPath
            }
        ]
    },
    {
        name: '仪表盘',
        path: '/dashboard',
        icon: SmileOutlined,
        testPath
    },
    {
        name: '数据模型',
        path: '/data_model',
        icon: SmileOutlined,
        testPath
    }
];

for (let i = 0; i < 5; ++i) {
    config.push({
        name: '测试' + i,
        path: '/test?v=' + i,
        // path: '/test' + i,
        icon: SmileOutlined,
        testPath
    });
}

function doProcess(targetConfig: MenuConfigItem[]) {
    targetConfig.forEach(configItem => {
        configItem.key = configItem.path;
        configItem.isStartPage = configItem.path === defaultStartPage;
        if (configItem.children) {
            doProcess(configItem.children);
        }
    });
}

function processConfig(targetConfig: MenuConfigItem[]) {
    doProcess(targetConfig);
    return targetConfig;
}

const asideMenuConfig = () => new Promise(resolve => {
    setTimeout(() => resolve(processConfig(config)), 500);
});

export {asideMenuConfig};
