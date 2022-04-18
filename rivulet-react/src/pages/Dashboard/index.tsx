import {useContext, useState} from 'react';
import {CachingNode} from '@/layouts/BasicLayout';
import {TabNodeContext, TabNodeContextType} from '@/layouts/BasicLayout/components/KeepAliveTabs/TabNodeProvider';

function Dashboard() {
    const {
        closeTab
    } = useContext<TabNodeContextType>(TabNodeContext);
    const [counter, setCounter] = useState(0);
    return (
        <div style={{margin: '20px'}}>
            <div>
                <h2>Dashboard page {counter}</h2>
            </div>
            <button onClick={() => setCounter(counter + 1)}>Add</button>
            <button onClick={() => closeTab()} style={{marginLeft: '3rem'}}>Close Tab</button>
        </div>
    );
}

export default () => {
    return (
        <CachingNode>
            <Dashboard/>
        </CachingNode>
    );
};
