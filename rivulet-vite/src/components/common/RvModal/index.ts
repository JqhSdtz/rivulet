import {Modal, ModalFuncProps} from 'antd';
import {Result} from '@/types/result';
import {useContext} from 'react';
import {TabNodeContext, TabNodeContextType} from '@/layouts/BasicLayout';

function presetProps(props: ModalFuncProps, tabNodeContext: TabNodeContextType) {
    const {tabNode} = tabNodeContext;
    if (tabNode?.splitView?.contentRef?.current) {
        props.getContainer = () => tabNode.splitView.contentRef.current;
    }
    props.wrapClassName = (props.wrapClassName ?? '') + ' rv-modal';
    props.maskStyle = {
        position: 'absolute'
    };
    props.maskClosable = false;
    props.centered = true;
    props.transitionName = props.transitionName ?? '';
    return props;
}

export const useRvModal = () => {
    const tabNodeContext = useContext(TabNodeContext);
    return {
        confirm(props: ModalFuncProps) {
            return Modal.confirm(presetProps(props, tabNodeContext));
        },
        success(props: ModalFuncProps) {
            return Modal.success(presetProps(props, tabNodeContext));
        },
        error(props: ModalFuncProps) {
            return Modal.error(presetProps(props, tabNodeContext));
        },
        result(result: Result) {
            if (result.successful) {
                return this.success({
                    content: result.returnMessage
                });
            } else {
                return this.error({
                    content: result.errorMessage
                });
            }
        }
    };
};
