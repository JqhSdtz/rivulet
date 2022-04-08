module.exports = {
    vite: true,
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
                    'layout-body-background': '#fafafa',
                    'tabs-card-head-background': '#fafafa'
                }
            }
        ]
    ],
    babelPlugins: [
        'react-activation/babel'
    ],
    proxy: {
        '/api': {
            enable: true,
            target: 'http://127.0.0.1:8081'
        }
    }
}
