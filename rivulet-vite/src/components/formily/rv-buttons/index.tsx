import {Link} from 'react-router-dom';
import {Button} from 'antd';
import {ButtonProps} from 'antd/lib/button/button';
import {useContext} from 'react';
import {TabNodeContext} from '@/layouts/BasicLayout';

type LinkButtonProps = {
    to: string
} & ButtonProps;
export const RvLinkButton = (props: LinkButtonProps) => {
    const {
        tabNode
    } = useContext(TabNodeContext);
    let path = props.to;
    if (props.to?.charAt(0) === '.') {
        path = tabNode.targetMenu?.path + props.to.substring(1);
    }
    return (
        <Link to={path}>
            <Button key="button" type="primary">
                新建
            </Button>
        </Link>
    );
};
