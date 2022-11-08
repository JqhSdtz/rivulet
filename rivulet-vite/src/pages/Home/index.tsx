import styles from './index.module.css';
import {Button} from 'antd';
import RvRequest from '@/utils/rvRequest';
import axios from 'axios';
import RvModal from '@/components/common/RvModal';

export default () => {
    const clearScriptCache = async () => {
        const result = await RvRequest.do(() => axios.post('/dataModel/clearScriptCache'));
        RvModal.result(result);
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
