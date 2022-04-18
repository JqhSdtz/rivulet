import {useLocation} from 'ice';
import {KeepAlive} from 'react-activation';

export interface CachingNodeType {
    createTime: number
    updateTime: number
    name?: string
    id: string

    [key: string]: any
}

export default (props) => {
    const {pathname, search} = useLocation();
    const tabKey = pathname + search;
    return (
        <KeepAlive
            name={tabKey}
            id={tabKey}
            saveScrollPosition="screen"
        >
            {props.children}
        </KeepAlive>
    );
}
