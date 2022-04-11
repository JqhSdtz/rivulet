import {useLocation} from 'ice';
import {KeepAlive} from 'react-activation';

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
    )
}
