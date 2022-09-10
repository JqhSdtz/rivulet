import {Link} from 'react-router-dom';
import {Button} from 'antd';
import {ButtonProps} from 'antd/lib/button/button';
import {useContext} from 'react';
import {TabNodeContext} from '@/layouts/BasicLayout';
import {RecursionField, Schema} from '@formily/react';
import {RvTableContext} from '@/components/formily';

type LinkButtonProps = {
    to: string
    text?: string
    icon?: Schema
    data?: any
} & ButtonProps;
export const RvLinkButton = (props: LinkButtonProps) => {
    const {
        tabNode
    } = useContext(TabNodeContext);
    let path = props.to;
    // 暂不支持'././xxx'这种写法
    if (props.to?.startsWith('./')) {
        path = tabNode.targetMenu?.path + props.to.substring(1);
    }
    path += '?_timestamp=' + Date.now();
    if (props.data) {
        const keys = Object.keys(props.data);
        keys.forEach(key => {
            path += '&' + key + '=' + props.data[key]
        });
    }
    return (
        <Link {...props} to={path}>
            {
                props.icon ? <RecursionField schema={props.icon}/> : (
                    <Button type="primary">
                        {props.text ?? '跳转'}
                    </Button>
                )
            }
        </Link>
    );
};

type QueryButtonProps = {
    text?: string
} & ButtonProps;
export const RvQueryButton = (props: QueryButtonProps) => {
    const {
        query
    } = useContext(RvTableContext);
    return (
        <Button type="primary" onClick={query} {...props}>
            {props.text ?? '查询'}
        </Button>
    );
};
