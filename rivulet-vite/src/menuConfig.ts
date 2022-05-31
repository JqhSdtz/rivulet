import {SmileFilled, SmileOutlined} from '@ant-design/icons';
import {MenuDataItem} from '@/layouts/BasicLayout';

export interface MenuConfigItem extends MenuDataItem {
    isStartPage?: boolean;
    testPath?: (path: string | undefined) => boolean;
    children?: MenuConfigItem[];
}

function testPath(this: MenuConfigItem, path) {
    if (!this.path) {
        return false;
    }
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
        testPath
    },
    {
        name: '测试子目录',
        icon: SmileOutlined,
        children: [
            {
                name: '测试2',
                path: '/test?v=2',
                icon: SmileFilled,
                testPath
            },
            {
                name: '测试子子目录',
                icon: SmileOutlined,
                children: [
                    {
                        name: '测试4',
                        path: '/test?v=4',
                        icon: SmileFilled,
                        testPath
                    }
                ]
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
    },
    {
        name: '测试5',
        path: '/test?v=5',
        icon: SmileOutlined,
        testPath
    },
    {
        name: '测试6',
        path: '/test?v=6',
        icon: SmileOutlined,
        testPath
    }
];

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
    // 模拟后台获取菜单
    setTimeout(() => resolve(processConfig(config)), 500);
});

export {asideMenuConfig};
