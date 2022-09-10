import {useContext} from 'react';
import {TabNodeContext} from '@/layouts/BasicLayout';
import {createForm, Form} from '@formily/core';

const formInstanceMap = new Map<string, Form>();

export const useFormInstance = () => {
    const {
        tabNode
    } = useContext(TabNodeContext);
    let formInstance = formInstanceMap.get(tabNode.name);
    if (!formInstance) {
        formInstance = createForm();
        formInstanceMap.set(tabNode.name, formInstance);
    }
    return formInstance;
}
