import {Link} from 'ice';
import {KeepAliveTab} from '@/layouts/BasicLayout';

const NotFound = () => {
    return (
        <div>
            <h2>404</h2>
            <div>
                <Link to="/">Home</Link>
            </div>
            <div>
                <Link to="/dashboard">Dashboard</Link>
            </div>
        </div>
    );
};

export default () => {
    return (
        <KeepAliveTab>
            <NotFound/>
        </KeepAliveTab>
    )
};
