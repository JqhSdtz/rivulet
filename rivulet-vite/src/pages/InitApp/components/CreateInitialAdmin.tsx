import React from 'react';
import {createForm} from '@formily/core';
import {createSchemaField} from '@formily/react';
import {Form, FormItem, Input, Password, Submit} from '@formily/antd';
import {Card} from 'antd';
import * as ICONS from '@ant-design/icons';
import {FormProps} from '@formily/antd/esm/form';
import md5 from 'md5';
import {useRvModal} from '@/components/common/RvModal';
import store from '@/store';
import axios from 'axios';
import {Result} from '@/types/result';
import RvRequest from '@/utils/rvRequest';

const form = createForm({
    validateFirst: true
});

const SchemaField = createSchemaField({
    components: {
        FormItem,
        Input,
        Password
    },
    scope: {
        icon(name) {
            return React.createElement(ICONS[name]);
        }
    }
});

const schema = {
    type: 'object',
    properties: {
        adminName: {
            type: 'string',
            title: '用户名',
            required: true,
            'x-decorator': 'FormItem',
            'x-component': 'Input',
            'x-component-props': {
                prefix: '{{icon(\'UserOutlined\')}}'
            },
            'x-validator': {
                triggerType: 'onBlur',
                minLength: 2,
                maxLength: 32
            }
        },
        password: {
            type: 'string',
            title: '密码',
            required: true,
            'x-decorator': 'FormItem',
            'x-component': 'Password',
            'x-component-props': {
                prefix: '{{icon(\'LockOutlined\')}}'
            },
            'x-validator': [
                {
                    triggerType: 'onBlur',
                    minLength: 8
                }
            ]
        }
    }
};

const formLayout: FormProps = {
    layout: 'vertical',
    size: 'large'
};

interface ICreateInitialAdminProps {
    onPass?: Function,
    onReject?: Function
}

export default (props: ICreateInitialAdminProps) => {
    const rvModal = useRvModal();
    const authDispatchers = store.useModelDispatchers('auth');
    const [loading, setLoading] = React.useState<boolean>(false);

    async function onSubmit(data) {
        setLoading(true);
        data.password = md5(data.password);
        const result: Result = await RvRequest.do(() => axios.post('/app/initialAdmin', data));
        setLoading(false);
        if (result.successful) {
            rvModal.success({
                content: '创建初始用户成功！',
                onOk() {
                    authDispatchers.setState({
                        hasLoggedIn: true
                    });
                    props.onPass?.();
                }
            });
        } else {
            rvModal.error({
                content: '创建初始用户失败！',
                onOk() {
                    props.onReject?.();
                }
            });
        }
    }

    return (
        <div
            style={{
                height: '100%',
                display: 'flex',
                justifyContent: 'center',
                padding: 'auto'
            }}
        >
            <Card
                style={{
                    width: 400,
                    height: 340,
                    marginTop: 90
                }}
                bodyStyle={{
                    padding: 40
                }}
            >
                <Form
                    form={form}
                    {...formLayout}
                    onAutoSubmit={onSubmit}
                >
                    <SchemaField schema={schema}/>
                    <Submit block size="large" loading={loading}>
                        创建用户
                    </Submit>
                </Form>
                <div
                    style={{
                        display: 'flex',
                        marginTop: 15,
                        justifyContent: 'flex-end'
                    }}
                >
                    <a href="#创建初始用户须知">创建初始用户须知</a>
                </div>
            </Card>
        </div>
    );
};
