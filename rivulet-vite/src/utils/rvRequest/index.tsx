import axios, {AxiosError, AxiosResponse} from 'axios';
import {Prism as SyntaxHighlighter} from 'react-syntax-highlighter';
import {Input, Modal} from 'antd';
import {useState} from 'react';
import {InputStatus} from 'antd/lib/_util/statusUtils';
import ReactDOM from 'react-dom';
import './index.less';
import {Result} from '@/types/result';
import {useRvModal} from '@/components/common/RvModal';

function wrapRequestFunction(oriRequest: () => Promise<AxiosResponse>): () => Promise<Result> {
    return () => oriRequest().then(response => {
        const successResult: Result = response.data;
        successResult.rawResponse = response;
        return Promise.resolve(successResult);
    }).catch((error: AxiosError) => {
        const errorResult: Result = {
            successful: false,
            rawResponse: error.response,
            errorCode: 'UnexpectedError',
            errorMessage: '网络或服务异常，请重试',
            rawError: error
        };
        return Promise.resolve(errorResult);
    });
}

const ConfirmUpdateSqlModal = (props: {
    sql: string,
    doRequest: () => Promise<Result>,
    onUpdateSucceed: () => void
}) => {
    const rvModal = useRvModal();
    const [displaySql, setDisplaySql] = useState<string>(props.sql);
    const [isModalOpen, setModalOpen] = useState<boolean>(true);
    const [confirmKey, setConfirmKey] = useState<string>();
    const [inputStatus, setInputStatus] = useState<InputStatus>('');

    async function onOk() {
        if (!confirmKey) {
            setInputStatus('error');
        } else {
            const request = () => axios.post('/builtInDataModel/confirmUpdateSql', {confirmKey});
            const confirmResult = await wrapRequestFunction(request)();
            if (confirmResult.successful) {
                setModalOpen(false);
                props.onUpdateSucceed();
            } else if (confirmResult.errorCode === 'requireConfirmUpdateSql') {
                setDisplaySql(confirmResult.payload);
            }
            rvModal.result(confirmResult);
        }
    };
    return (
        <Modal
            className="confirm-sql-modal"
            closable={false}
            open={isModalOpen}
            width="90%"
            title="确认执行SQL"
            okText="确认执行"
            cancelButtonProps={{hidden: true}}
            onOk={onOk}
        >
            <SyntaxHighlighter language="sql" wrapLongLines={true}>
                {displaySql}
            </SyntaxHighlighter>
            <Input placeholder="请输入确认密钥" status={inputStatus} maxLength={64} value={confirmKey}
                   onChange={e => setConfirmKey(e.target.value)}/>
        </Modal>
    );
};


async function onRequireConfirmUpdateSql(result: Result, doRequest: () => Promise<Result>) {
    const currentStructureUpdateSql = result.payload;
    let curResolve, curReject;
    const onUpdateSucceed = () => {
        doRequest().then(curResolve).catch(curReject);
    };
    const modal = <ConfirmUpdateSqlModal
        sql={currentStructureUpdateSql}
        doRequest={doRequest}
        onUpdateSucceed={onUpdateSucceed}
    />;
    const container = document.createDocumentFragment();
    ReactDOM.render(modal, container);
    return new Promise<Result>((resolve, reject) => {
        curResolve = resolve;
        curReject = reject;
    });
}

export default {
    async doRaw(requestFunc: () => Promise<AxiosResponse>): Promise<AxiosResponse> {
        const result = await this.do(requestFunc);
        return result.rawResponse;
    },
    async do(requestFunc: () => Promise<AxiosResponse>): Promise<Result> {
        const doRvRequest = wrapRequestFunction(requestFunc);
        let result = await doRvRequest();
        if (result.successful) {
            return result;
        } else {
            const errorCode = result.errorCode;
            if (errorCode === 'requireConfirmUpdateSql') {
                result = await onRequireConfirmUpdateSql(result, doRvRequest);
            }
        }
        return result;
    }
};
