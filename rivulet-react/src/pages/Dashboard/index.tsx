import {useContext, useState} from 'react';
import {TabNodeProvider} from '@/layouts/BasicLayout';
import {TabNodeContext, TabNodeContextType} from '@/layouts/BasicLayout';
import {useActivate} from 'react-activation';

const Dashboard = () => {
    const {
        closeTab,
        beforeClose
    } = useContext<TabNodeContextType>(TabNodeContext);
    beforeClose((clearAttention, doClose) => {
        setTimeout(clearAttention, 3000);
        setTimeout(doClose, 4000);
        return false;
    });
    const [counter, setCounter] = useState(0);
    const [activeCounter, setActiveCounter] = useState(0);
    useActivate(() => {
        setActiveCounter(activeCounter + 1);
    })
    return (
        <div style={{margin: '20px'}}>
            <div>
                <h2>Dashboard page {counter}</h2>
                <h3>Dashboard page has been activated {activeCounter} times</h3>
            </div>
            <button onClick={() => setCounter(counter + 1)}>Add</button>
            <button onClick={() => closeTab()} style={{marginLeft: '3rem'}}>Close Tab</button>
        </div>
    );
}

export default () => {
    return (
        <TabNodeProvider>
            <Dashboard/>
        </TabNodeProvider>
    );
};
