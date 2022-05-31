module.exports = {
    vite: true,
    sourceMap: false,
    plugins: [
        [
            'build-plugin-ignore-style',
            {
                libraryName: 'antd'
            }
        ],
        [
            'build-plugin-antd',
            {
                disableModularImport: true,
                themeConfig: {
                    // 'tabs-card-height': '40px',
                    'layout-body-background': '#ffffff',
                    'tabs-card-head-background': '#f0f0f5'
                }
            }
        ]
    ],
    proxy: {
        '/api': {
            enable: true,
            target: 'http://127.0.0.1:8081'
        }
    }
};
