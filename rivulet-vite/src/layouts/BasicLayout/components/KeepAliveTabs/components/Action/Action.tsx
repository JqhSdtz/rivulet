import React, {CSSProperties} from 'react';
import classNames from 'classnames';

import './Action.less';

export interface Props extends React.HTMLAttributes<HTMLButtonElement> {
    active?: {
        fill: string;
        background: string;
    };
    cursor?: CSSProperties['cursor'];
}

export function Action({active, className, cursor, style, ...props}: Props) {
    return (
        <button
            {...props}
            className={classNames('keep-alive-sort-action', className)}
            tabIndex={0}
            style={
                {
                    ...style,
                    cursor,
                    '--fill': active?.fill,
                    '--background': active?.background
                } as CSSProperties
            }
        />
    );
}
