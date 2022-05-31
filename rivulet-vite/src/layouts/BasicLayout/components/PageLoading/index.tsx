import React from 'react';
import type {SpinProps} from 'antd';
import {Spin} from 'antd';

const PageLoading: React.FC<SpinProps & any> = ({
                                                    isLoading,
                                                    pastDelay,
                                                    timedOut,
                                                    error,
                                                    retry,
                                                    ...reset
                                                }) => (
    <div style={{paddingTop: 100, textAlign: 'center'}}>
        <Spin size="large" {...reset} />
    </div>
);

export default PageLoading;
