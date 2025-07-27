import axios, {AxiosError, AxiosResponse} from 'axios';
import {Prism as SyntaxHighlighter} from 'react-syntax-highlighter';
import {Input, Modal} from 'antd';
import {useState} from 'react';
import {InputStatus} from 'antd/lib/_util/statusUtils';
import ReactDOM from 'react-dom';
import './index.less';
import {Result} from '@/types/result';
import {useRvModal} from '@/components/common/RvModal';
import rvUtil from "@/utils/rvUtil";
import {getRvScope} from "@/utils/formilyUtil";

function wrapRequestFunction(oriRequest: () => Promise<AxiosResponse>): () => Promise<Result> {
    return () =>
        oriRequest()
            .then(response => {
                const successResult: Result = response.data;
                successResult.rawResponse = response;
                return Promise.resolve(successResult);
            })
            .catch((error: AxiosError) => {
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

const ConfirmUpdateSqlModal = (props: {sql: string; doRequest: () => Promise<Result>; onUpdateSucceed: () => void}) => {
    const rvModal = useRvModal();
    const [displaySql, setDisplaySql] = useState<string>(props.sql);
    const [isModalOpen, setModalOpen] = useState<boolean>(true);
    const [confirmKey, setConfirmKey] = useState<string>();
    const [inputStatus, setInputStatus] = useState<InputStatus>('');
    const [loading, setLoading] = useState<boolean>(false);

    async function onOk() {
        if (!confirmKey) {
            setInputStatus('error');
        } else {
            setLoading(true);
            const request = () => axios.post('/builtInDataModel/confirmUpdateSql', {confirmKey});
            const confirmResult = await wrapRequestFunction(request)();
            if (confirmResult.successful) {
                setLoading(false);
                setModalOpen(false);
                props.onUpdateSucceed();
            } else if (confirmResult.errorCode === 'requireConfirmUpdateSql') {
                setDisplaySql(confirmResult.payload);
            }
            rvModal.result(confirmResult);
        }
    }

    return (
        <Modal
            className="confirm-sql-modal"
            closable={false}
            open={isModalOpen}
            width="90%"
            title="确认执行SQL"
            okText="确认执行"
            cancelButtonProps={{hidden: true}}
            okButtonProps={{loading}}
            onOk={onOk}
        >
            <SyntaxHighlighter language="sql" wrapLongLines={true}>
                {displaySql}
            </SyntaxHighlighter>
            <Input
                placeholder="请输入确认密钥"
                status={inputStatus}
                maxLength={64}
                value={confirmKey}
                onChange={e => setConfirmKey(e.target.value)}
            />
        </Modal>
    );
};

async function onRequireConfirmUpdateSql(result: Result, doRequest: () => Promise<Result>) {
    const currentStructureUpdateSql = result.payload;
    let curResolve, curReject;
    const onUpdateSucceed = () => {
        doRequest().then(curResolve).catch(curReject);
    };
    const modal = (
        <ConfirmUpdateSqlModal
            sql={currentStructureUpdateSql}
            doRequest={doRequest}
            onUpdateSucceed={onUpdateSucceed}
        />
    );
    const container = document.createDocumentFragment();
    ReactDOM.render(modal, container);
    return new Promise<Result>((resolve, reject) => {
        curResolve = resolve;
        curReject = reject;
    });
}

export default {
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
    },
    async doPost(url: string, data: any): Promise<Result> {
        return this.do(() => axios.post(url, data));
    },
    async doPostRaw(url: string, data: any): Promise<Result> {
        return this.doRaw(() => axios.post(url, data));
    },
    async doRaw(requestFunc: () => Promise<AxiosResponse>): Promise<AxiosResponse> {
        const result = await this.do(requestFunc);
        return result.rawResponse;
    },
    async runJs(filename: string, data: any, withTransaction: boolean) {
        const baseUrl = withTransaction ? '/js/runWithTransaction' : '/js/run';
        return await this.doRaw(() => axios.post(baseUrl + '?filename=' + filename, data));
    },
    async runJsSchema(filename: string, data: any = {}) {
        const rawResponse = await this.runJs('/src/schemas/' + filename, data, false);
        const result = rawResponse.data;
        const deepTranslate = _root => {
            if (typeof _root === 'string' && _root.length > 7 && _root.substring(0, 7) === '$RvFun$') {
                return eval(_root.substring(7, _root.length));
            } else if (typeof _root === 'object') {
                for (const key in _root) {
                    _root[key] = deepTranslate(_root[key]);
                }
            }
            return _root;
        }
        result.payload = deepTranslate(result.payload);
        // 这里是为schema中通过wrapFunctionToStr包裹的函数传递上下文，因为其函数最终都是在当前上下文中执行，所以直接通过this就可以设置上下文
        this.$rvScope = getRvScope(result.payload.rvInjected);
        return rawResponse;
    },
    async runJsServiceRaw(filename: string, data: any = {}): Promise<Result> {
        return this.runJs('/src/services/' + filename, data, true);
    },
    async runJsService(filename: string, data: any = {}): Promise<Result> {
        const rawResponse = await this.runJsServiceRaw(filename, data);
        return rawResponse.data;
    },
    async clearJsCache() {
        return await this.doRaw(() => axios.post('/js/clearCache'));
    }
};
