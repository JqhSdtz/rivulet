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
                    // // @layout-body-background: #f0f2f5
                    // 'tabs-card-head-background': '#f0f2f5'
                }
            }
        ]
    ],
    babelPlugins: [
        'react-activation/babel'
    ]
}
