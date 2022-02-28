import {useAliveController} from 'react-activation';

import KeepAliveTab from './KeepAliveTab';
import styles from './index.module.less';
import React from 'react';
import {HeaderViewProps} from '@/layouts/BasicLayout/Header';

const KeepAliveTabs: React.FC<HeaderViewProps> = () => {
    const {getCachingNodes} = useAliveController();
    const cachingNodes = getCachingNodes();
    return (
        <ul className={styles['alive-tabs']}>
            {cachingNodes.map((node, idx) => (
                <KeepAliveTab key={idx} node={node} />
            ))}
        </ul>
    );
};

export default KeepAliveTabs;
