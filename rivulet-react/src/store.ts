// src/store.ts
import {createStore} from 'ice';
import user from './models/user';
import app from '@/models/app';

const store = createStore(
    {
        user,
        app,
    },
    {
        // options
    },
);

export default store;
