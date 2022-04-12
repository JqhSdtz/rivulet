import {Link} from 'ice';
import {CachingNode} from '@/layouts/BasicLayout';

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
        <CachingNode>
            <NotFound/>
        </CachingNode>
    )
};
