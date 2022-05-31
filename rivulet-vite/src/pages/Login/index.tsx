import React from 'react';
import {createForm} from '@formily/core';
import {createSchemaField} from '@formily/react';
import {Form, FormItem, Input, Password, Submit} from '@formily/antd';
import {Card} from 'antd';
import * as ICONS from '@ant-design/icons';
import {FormProps} from '@formily/antd/esm/form';
import store from '@/store';
import md5 from 'md5';
import RvModal from '@/components/Common/RvModal';
import axios from 'axios';

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
                prefix: '{{icon(\'UserOutlined\')}}',
                autoFocus: true
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
            }
        }
    }
};

const formLayout: FormProps = {
    layout: 'vertical',
    size: 'large'
};

export default () => {
    const userDispatchers = store.useModelDispatchers('user');
    const authDispatchers = store.useModelDispatchers('auth');

    async function onSubmit(data) {
        data.password = md5(data.password);
        const response = await axios.post('/auth/login', data);
        const result: Result = response.data;
        if (result.successful) {
            userDispatchers.setState(result.payload);
            RvModal.success({
                content: '登录成功！',
                onOk() {
                    authDispatchers.setState({
                        hasLoggedIn: true
                    });
                }
            });
        } else {
            RvModal.error({
                content: '登录失败！' + result.errorMessage
            });
        }
    }

    return (
        <div
            style={{
                height: '100vh',
                display: 'flex',
                justifyContent: 'center',
                background: '#eee',
                padding: 'auto'
            }}
        >
            <Card
                style={{
                    width: 400,
                    height: 340,
                    marginTop: 'calc(50vh - 170px)'
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
                        登录
                    </Submit>
                </Form>
                <div
                    style={{
                        display: 'flex',
                        marginTop: 15,
                        justifyContent: 'flex-end'
                    }}
                >
                    <a href="#忘记密码">忘记密码?</a>
                </div>
            </Card>
        </div>
    );
};
