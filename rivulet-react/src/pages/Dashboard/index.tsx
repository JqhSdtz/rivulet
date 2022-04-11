import {useState} from 'react';
import {KeepAliveTab} from '@/layouts/BasicLayout';

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
    return (
        <KeepAliveTab>
            <Dashboard/>
        </KeepAliveTab>
    );
};
