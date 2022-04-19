import React from 'react';
import {createForm} from '@formily/core';
import {createSchemaField} from '@formily/react';
import {Form, FormItem, Input, Password, Submit} from '@formily/antd';
import {Card} from 'antd';
import * as ICONS from '@ant-design/icons';
import {FormProps} from '@formily/antd/esm/form';
import {request} from 'ice';
import RvModal from '@/components/Common/RvModal';

const form = createForm();

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
        initKey: {
            type: 'string',
            title: '初始密钥',
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

interface IVerifyInitKeyProps {
    onPass?: Function,
    onReject?: Function
}

export default (props: IVerifyInitKeyProps) => {
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
                    height: 250,
                    marginTop: 90
                }}
                bodyStyle={{
                    padding: 40
                }}
            >
                <Form
                    form={form}
                    {...formLayout}
                    onAutoSubmit={async (data) => {
                        const result: Result = await request.post('/app/verifyInitKey', data);
                        if (result.successful) {
                            RvModal.success({
                                content: '密钥验证成功！',
                                onOk() {
                                    props.onPass?.();
                                }
                            });
                        } else {
                            RvModal.error({
                                content: '密钥验证失败！请检查密钥是否已过期。',
                                onOk() {
                                    props.onReject?.();
                                }
                            });
                        }
                    }}
                >
                    <SchemaField schema={schema}/>
                    <Submit block size="large">
                        验证密钥
                    </Submit>
                </Form>
                <div
                    style={{
                        display: 'flex',
                        marginTop: 15,
                        justifyContent: 'flex-end'
                    }}
                >
                    <a href="#如何获取密钥">如何获取密钥</a>
                </div>
            </Card>
        </div>
    );
};
