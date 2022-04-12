import React from 'react';
import {createForm} from '@formily/core';
import {createSchemaField} from '@formily/react';
import {Form, FormItem, Input, Password, Submit} from '@formily/antd';
import {Card, Modal} from 'antd';
import * as ICONS from '@ant-design/icons';
import {FormProps} from '@formily/antd/esm/form';
import md5 from 'md5';
import {request} from 'ice';

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
            return React.createElement(ICONS[name])
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
                prefix: '{{icon(\'UserOutlined\')}}',
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
                prefix: '{{icon(\'LockOutlined\')}}',
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

interface ICreateIntialUserProps {
    onPass?: Function,
    onReject?: Function
}

export default (props: ICreateIntialUserProps) => {
    async function onSubmit(data) {
        data.password = md5(data.password);
        const result: Result = await request.post('/app/initialUser', data);
        if (result.successful) {
            Modal.success({
                content: '创建初始用户成功！',
                onOk() {
                    props.onPass?.();
                }
            });
        } else {
            Modal.error({
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
                        justifyContent: 'flex-end',
                    }}
                >
                    <a href="#创建初始用户须知">创建初始用户须知</a>
                </div>
            </Card>
        </div>
    );
};