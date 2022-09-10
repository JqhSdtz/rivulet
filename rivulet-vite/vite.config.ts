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
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:8081'
            }
        }
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
    esbuild: {
        logOverride: {'this-is-undefined-in-esm': 'silent'}
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
