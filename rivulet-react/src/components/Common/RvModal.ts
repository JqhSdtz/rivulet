import {Modal, ModalFuncProps} from 'antd';

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
    }
}
