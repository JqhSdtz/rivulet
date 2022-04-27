module.exports = {
    vite: true,
    sourceMap: true,
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
    // babelPlugins: [
    //     'react-activation-local/babel'
    // ],
    proxy: {
        '/api': {
            enable: true,
            target: 'http://127.0.0.1:8081'
        }
    }
}
