import {SmileFilled, SmileOutlined} from '@ant-design/icons';
import {MenuDataItem} from '@/layouts/BasicLayout';

export interface MenuConfigItem extends MenuDataItem {
    testPath?: (path: string | undefined) => boolean;
    parent?: MenuConfigItem;
    children?: MenuConfigItem[];
    isHidden?: boolean;
    hiddenChildren?: MenuConfigItem[];
}

function testPath(this: MenuConfigItem, path) {
    if (!path) path = '/';
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

const config: MenuConfigItem[] = [
    {
        name: '首页',
        path: '/',
        icon: <SmileOutlined/>,
        testPath
    },
    {
        name: '测试子目录',
        // 父目录也要有path，不然会出错
        path: '/test_child',
        icon: <SmileFilled/>,
        children: [
            {
                name: '测试2',
                path: '/test?v=2',
                icon: <SmileFilled/>,
                testPath
            },
            {
                name: '测试子子目录',
                icon: <SmileOutlined/>,
                path: '/test_child_child',
                children: [
                    {
                        name: '测试4',
                        path: '/test?v=4',
                        icon: <SmileFilled/>,
                        testPath
                    }
                ]
            }
        ]
    },
    {
        name: '仪表盘',
        path: '/dashboard',
        icon: <SmileOutlined/>,
        testPath
    },
    {
        name: '数据模型',
        path: '/dataModel',
        icon: <SmileOutlined/>,
        testPath,
        hiddenChildren: [
            {
                name: '数据模型编辑',
                path: 'detail',
                icon: <SmileOutlined/>,
                testPath
            }
        ]
    },
    {
        name: '测试5',
        path: '/test?v=5',
        icon: <SmileOutlined/>,
        testPath
    },
    {
        name: '测试6',
        path: '/test?v=6',
        icon: <SmileOutlined/>,
        testPath
    }
];

function doProcess(targetConfig: MenuConfigItem[]) {
    targetConfig.forEach(configItem => {
        configItem.key = configItem.path;
        if (configItem.children) {
            configItem.children.forEach(childItem => {
                childItem.parent = configItem;
            });
            doProcess(configItem.children);
        }
        if (configItem.hiddenChildren) {
            configItem.hiddenChildren.forEach(childItem => {
                childItem.parent = configItem;
                childItem.path = configItem.path + '/' + childItem.path;
                childItem.isHidden = true;
            });
            doProcess(configItem.hiddenChildren);
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
