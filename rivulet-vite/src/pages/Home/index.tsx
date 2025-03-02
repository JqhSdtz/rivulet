import styles from './index.module.css';
import {Button} from 'antd';
import RvRequest from '@/utils/rvRequest';
import axios from 'axios';
import {useRvModal} from '@/components/common/RvModal';
import {useRequest} from 'ahooks';
import {useState} from 'react';

export default () => {
    const rvModal = useRvModal();
    const [reqLoading, setReqLoading] = useState<boolean>(true);
    const clearScriptCache = async () => {
        const {data, loading} = useRequest(() => RvRequest.clearJsCache());
        setReqLoading(loading);
        rvModal.result(data);
    };
    return (
        <>
            <div className={styles.container}>
                <h2>Home page</h2>
            </div>
            <Button style={{marginLeft: '1rem'}} onClick={clearScriptCache} loading={reqLoading}>
                清除脚本缓存
            </Button>
        </>
    );
};
