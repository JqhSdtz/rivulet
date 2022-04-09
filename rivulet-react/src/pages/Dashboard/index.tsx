import {KeepAlive} from 'react-activation';
import {useLocation} from 'ice';
import {useContext, useState} from 'react';
import RouteContext from '@/layouts/BasicLayout/contexts/RouteContext';

function Dashboard() {
    const [counter, setCounter] = useState(0);
    return (
        <div>
            <div>
                <h2>Dashboard page {counter}</h2>
            </div>
            <button onClick={() => setCounter(counter + 1)}>Add</button>
        </div>
    );
}

export default () => {
    const location = useLocation();
    const {matchMenus} = useContext(RouteContext);
    return (
        <KeepAlive
            name={location.pathname}
            id={location.pathname}
            matchMenus={matchMenus}
            saveScrollPosition="screen"
        >
            <Dashboard />
        </KeepAlive>
    );
};
