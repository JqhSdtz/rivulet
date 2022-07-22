import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';
import vitePluginImp from 'vite-plugin-imp';
import * as path from 'path';
import reactNodeKey from 'react-node-key/vite';
import {autoFixContext} from 'react-activation';

autoFixContext(
    [require('react/jsx-runtime'), 'jsx', 'jsxs', 'jsxDEV'],
    [require('react/jsx-dev-runtime'), 'jsx', 'jsxs', 'jsxDEV']
);

export default defineConfig({
    build: {
        sourcemap: true
    },
    resolve: {
        alias: [
            {
                find: /^~/,
                replacement: ''
            },
            {
                find: '@',
                replacement: path.resolve(__dirname, './src')
            }
        ]
    },
    css: {
        preprocessorOptions: {
            less: {
                javascriptEnabled: true,
                modifyVars: {
                    'tabs-card-head-background': '#f0f0f5',
                    'layout-body-background': '#ffffff'
                }
            }
        }
    },
    server: {
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:8081'
            }
        }
    },
    plugins: [
        react(),
        reactNodeKey(),
        vitePluginImp({
            libList: [
                {
                    libName: 'antd',
                    style: (name) => `antd/lib/${name}/style/index.less`
                }
            ]
        })
    ]
});
