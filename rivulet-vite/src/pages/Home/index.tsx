import styles from './index.module.css';
import {Button} from 'antd';
import RvRequest from '@/utils/rvRequest';
import axios from 'axios';
import {useRvModal} from '@/components/common/RvModal';

export default () => {
    const rvModal = useRvModal();
    const clearScriptCache = async () => {
        const result = await RvRequest.clearJsCache();
        rvModal.result(result);
    }
    return (
        <>
            <div className={styles.container}>
                <h2>Home page</h2>
            </div>
            <Button
                style={{marginLeft: '1rem'}}
                onClick={clearScriptCache}>
                清除脚本缓存
            </Button>
        </>
    );
};
