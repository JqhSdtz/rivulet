import {Modal, ModalFuncProps} from 'antd';
import {Result} from '@/types/result';

function presetProps(props: ModalFuncProps) {
    props.transitionName = props.transitionName ?? '';
    return props;
}

export default {
    confirm(props: ModalFuncProps) {
        return Modal.confirm(presetProps(props));
    },
    success(props: ModalFuncProps) {
        return Modal.success(presetProps(props));
    },
    error(props: ModalFuncProps) {
        return Modal.error(presetProps(props));
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
