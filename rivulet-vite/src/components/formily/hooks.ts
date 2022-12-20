import {useContext} from 'react';
import {TabNodeContext} from '@/layouts/BasicLayout';
import {createForm, Form} from '@formily/core';
import {useCreation} from 'ahooks';

const formInstanceMap = new Map<string, Form>();

export const useFormInstance = (options?: IFormProps) => {
    const {
        tabNode
    } = useContext(TabNodeContext);
    return useCreation(() => {
        let formInstance = formInstanceMap.get(tabNode.name);
        if (!formInstance) {
            formInstance = createForm(options);
            formInstanceMap.set(tabNode.name, formInstance);
        }
        return formInstance;
    }, []);
};