import React from 'react';
import './global.less';
import App from '@/App';
import axios from 'axios';
import ReactDOM from 'react-dom';

axios.defaults.baseURL = '/api';

const root = (
    <App/>
);
ReactDOM.render(root, document.getElementById('root'));
