import React from 'react';
import './global.less';
import App from '@/App';
import axios from 'axios';
import remHelper from '@/utils/remHelper';
import ReactDOM from 'react-dom';
import moment from 'moment';

moment.locale('zh-cn');

axios.defaults.baseURL = '/api';

remHelper.initRem({
    pcWidth: 1280,
    maxFontSize: 18,
    pcOnly: true,
    responsive: true
});

const root = (
    <App/>
);
ReactDOM.render(root, document.getElementById('root'));
