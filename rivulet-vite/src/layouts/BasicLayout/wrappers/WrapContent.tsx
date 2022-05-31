import type {CSSProperties} from 'react';
import React from 'react';
import {Layout} from 'antd';
import {ConfigProviderWrap} from '@ant-design/pro-provider';
import {ErrorBoundary} from '@ant-design/pro-utils';

const WrapContent: React.FC<{
    autoClearCache?: boolean;
    // eslint-disable-next-line react/no-unused-prop-types
    isChildrenLayout?: boolean;
    className?: string;
    style?: CSSProperties;
    // eslint-disable-next-line react/no-unused-prop-types
    location?: any;
    // eslint-disable-next-line react/no-unused-prop-types
    contentHeight?: number | string;
    ErrorBoundary?: any;
    children?: React.ReactNode;
}> = props => {
    const {autoClearCache = true, style, className, children} = props;
    const ErrorComponent = props.ErrorBoundary || ErrorBoundary;
    return (
        <ConfigProviderWrap autoClearCache={autoClearCache}>
            {props.ErrorBoundary === false ? (
                <Layout.Content className={className} style={style}>
                    {children}
                </Layout.Content>
            ) : (
                <ErrorComponent>
                    <Layout.Content className={className} style={style}>
                        {children}
                    </Layout.Content>
                </ErrorComponent>
            )}
        </ConfigProviderWrap>
    );
};

export default WrapContent;
