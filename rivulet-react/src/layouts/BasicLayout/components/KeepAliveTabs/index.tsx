import React, {useContext} from 'react';
import {HeaderViewProps} from '../Header';
import './index.less';
import TabBarContainer from '@/layouts/BasicLayout/components/KeepAliveTabs/TabBarContainer';
import {TabsContext} from '@/layouts/BasicLayout';

// 提示：cachingNodes里的node的name是location的pathname+search
// 同时，TabNode的key和cachingNodes里的node的name相同
const KeepAliveTabs: React.FC<HeaderViewProps> = () => {
    const {keepAliveTabsElemRef} = useContext(TabsContext);
    return (
        <div className="keep-alive-tabs"
             ref={keepAliveTabsElemRef}
             style={{display: 'inline-flex', width: '100%'}}>
            <TabBarContainer/>
        </div>
    );
};

export default KeepAliveTabs;

export {default as TabNodeProvider} from './TabNodeProvider';
export {default as TabsContextProvider} from './TabsContextProvider';
export {default as TabsContent} from './TabsContent';
export * from './TabsContextProvider';
export * from './TabNodeProvider';
