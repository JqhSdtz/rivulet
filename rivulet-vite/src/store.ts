import {createStore} from '@ice/store';
import admin from '@/models/admin';
import app from '@/models/app';
import auth from '@/models/auth';

const store = createStore(
    {
        admin,
        app,
        auth
    },
    {}
);

export default store;
