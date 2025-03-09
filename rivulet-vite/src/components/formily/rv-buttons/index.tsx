import {Link} from 'react-router-dom';
import {Button} from 'antd';
import {ButtonProps} from 'antd/lib/button/button';
import React, {useContext, useState} from 'react';
import {TabNodeContext} from '@/layouts/BasicLayout';
import {RecursionField, Schema, useForm, useParentForm} from '@formily/react';
import {RvTableContext} from '@/components/formily';
import {useRvModal} from '@/components/common/RvModal';
import RvRequest from '@/utils/rvRequest';
import axios from 'axios';
import {Form, ObjectField} from '@formily/core';
import {Result} from '@/types/result';

type LinkButtonProps = {
    to: string;
    text?: string;
    icon?: Schema;
    data?: any;
} & ButtonProps;
export const RvLinkButton = (props: LinkButtonProps) => {
    const {tabNode} = useContext(TabNodeContext);
    let path = props.to;
    // 暂不支持'././xxx'这种写法
    if (props.to?.startsWith('./')) {
        path = tabNode.targetMenu?.path + props.to.substring(1);
    }
    const separator = path.indexOf('?') > -1 ? '&' : '?';
    path += separator + '_timestamp=' + Date.now();
    if (props.data) {
        const keys = Object.keys(props.data);
        keys.forEach(key => {
            path += '&' + key + '=' + props.data[key];
        });
    }
    return (
        <Link {...props} to={path}>
            {props.icon ? (
                <RecursionField schema={props.icon} />
            ) : (
                <Button type="primary">{props.text ?? '跳转'}</Button>
            )}
        </Link>
    );
};

type QueryButtonProps = {
    text?: string;
} & ButtonProps;
export const RvQueryButton = (props: QueryButtonProps) => {
    const {query} = useContext(RvTableContext);
    return (
        <Button type="primary" onClick={() => query()} {...props}>
            {props.text ?? '查询'}
        </Button>
    );
};

type SubmitButtonProps = {
    requestType: string;
    url: string;
} & ButtonProps;
export const RvSubmitButton = (props: SubmitButtonProps) => {
    const form = useForm();
    const rvModal = useRvModal();
    let [isLoading, setLoading] = useState(false);
    const onClick = () => {
        console.log(form);
        form.validate()
            .then(() => {
                setLoading(true);
                let action: (data: any) => Promise<Result>;
                if (props.requestType === 'js') {
                    action = data => RvRequest.runJsService(props.url, data);
                } else {
                    action = data => RvRequest.doPost(props.url, data);
                }
                form.submit(action)
                    .then(() => setLoading(false))
                    .then(() => rvModal.success({content: '保存成功'}));
            })
            .catch(() => {
                rvModal.error({content: '未知错误'});
            });
    };
    return (
        <Button type="primary" onClick={onClick} loading={isLoading}>
            提交
        </Button>
    );
};

type RequestButtonProps = {
    text: string;
    beforeRequest?: (form: Form | ObjectField) => boolean;
} & ButtonProps;
export const RvRequestButton = (props: RequestButtonProps) => {
    const form = useParentForm();
    const rvModal = useRvModal();
    let [isLoading, setLoading] = useState(false);
    const onClick = () => {
        if (props.beforeRequest && !props.beforeRequest(form)) return;
    };
    return (
        <Button type="primary" onClick={onClick} loading={isLoading}>
            {props.text}
        </Button>
    );
};
