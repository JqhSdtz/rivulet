import React from 'react';
import {createForm} from '@formily/core';
import {createSchemaField} from '@formily/react';
import {Form, FormItem, Input, Password, Submit} from '@formily/antd';
import {Card} from 'antd';
import * as ICONS from '@ant-design/icons';
import {FormProps} from '@formily/antd/esm/form';
import md5 from 'md5';
import RvModal from '@/components/common/RvModal';
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
        username: {
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

interface ICreateInitialUserProps {
    onPass?: Function,
    onReject?: Function
}

export default (props: ICreateInitialUserProps) => {
    const authDispatchers = store.useModelDispatchers('auth');

    async function onSubmit(data) {
        data.password = md5(data.password);
        const result: Result = await RvRequest.do(() => axios.post('/app/initialUser', data));
        if (result.successful) {
            RvModal.success({
                content: '创建初始用户成功！',
                onOk() {
                    authDispatchers.setState({
                        hasLoggedIn: true
                    });
                    props.onPass?.();
                }
            });
        } else {
            RvModal.error({
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
                    <Submit block size="large">
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
