import {ReactNode, useState} from 'react';
import {useMemoizedFn} from 'ahooks';

interface MouseOverProps {
    className?: string,
    onMouseOver?: ReactNode,
    onMouseOverClassName?: string,
    normal: ReactNode
}

export default (props: MouseOverProps) => {
    const [isMouseOver, setMouseOver] = useState(false);
    const targetComponent = (isMouseOver && props.onMouseOver) ? props.onMouseOver : props.normal;
    let className = props.className ?? '';
    if (isMouseOver && props.onMouseOverClassName) {
        className += ' ' + props.onMouseOverClassName;
    }
    const onMouseOver = useMemoizedFn(() => {
        setMouseOver(true);
    });
    const onMouseOut = useMemoizedFn(() => {
        setMouseOver(false);
    });
    // pointerEvent: none 用来防止鼠标在元素边缘时出现的闪烁现象
    return (
        <div className={className} onMouseOver={onMouseOver} onMouseOut={onMouseOut}>
            <div style={{pointerEvents: 'none'}}>{targetComponent}</div>
        </div>
    );
}
