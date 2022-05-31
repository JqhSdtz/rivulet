import {createStore} from '@ice/store';
import user from '@/models/user';
import app from '@/models/app';
import auth from '@/models/auth';

const store = createStore(
    {
        user,
        app,
        auth
    },
    {}
);

export default store;
