import {useEffect, useRef, useState} from 'react';
import axios from 'axios';
import {Modal} from 'antd';
import {useSize} from 'ahooks';
import './index.less';

const modalClassName = 'html-modal';

type IframeWindowType = {
    reloadIframe: () => void
} & WindowProxy;

const IframeContent = (props: {
    htmlContent: string,
    setContentWindow: (contentWindow: IframeWindowType) => void
}) => {
    const ref = useRef<HTMLIFrameElement>();
    useEffect(() => {
        ref.current.contentWindow.document.write(props.htmlContent);
        props.setContentWindow(ref.current.contentWindow as IframeWindowType);
    }, [props.htmlContent]);
    const size = useSize(ref.current?.parentNode?.parentNode as any);
    // modal的header高度为39，如果header高度调整，这里也要跟着调整
    return <iframe ref={ref} width={size?.width ?? 0} height={(size?.height ?? 0) - 39}/>;
};

export default () => {
    const [htmlContent, setHtmlContent] = useState<string>();
    const [isModalOpen, setModalOpen] = useState<boolean>(false);
    const [contentWindow, setContentWindow] = useState<IframeWindowType>();
    const [prevResponse, setPrevResponse] = useState<any>();
    useEffect(() => {
        axios.interceptors.response.use(function (response) {
            const contentType = response.headers['content-type'];
            if (contentType.indexOf('html') !== -1) {
                setHtmlContent(response.data);
                setModalOpen(true);
                setPrevResponse(response);
            }
            return response;
        });
    }, []);
    useEffect(() => {
        if (contentWindow) {
            contentWindow.reloadIframe = () => {
                // 原本的header中有个Symbol(default)，里面有个函数，会导致给header赋值时报错，把它过滤掉
                const tempHeader = {};
                for (let key in prevResponse.config.headers) {
                    tempHeader[key] = prevResponse.config.headers[key];
                }
                prevResponse.config.headers = tempHeader;
                axios(prevResponse.config);
            };
        }
    }, [contentWindow, prevResponse]);
    return (
        <Modal
            title={contentWindow?.document.title}
            open={isModalOpen}
            onCancel={() => setModalOpen(false)}
            className={modalClassName}
            width="90%"
            closable
            footer={null}
        >
            <IframeContent
                htmlContent={htmlContent}
                setContentWindow={setContentWindow}
            />
        </Modal>
    );
};
