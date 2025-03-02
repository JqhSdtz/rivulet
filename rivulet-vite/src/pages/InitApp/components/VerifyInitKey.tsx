import React, {useState} from 'react';
import {createForm} from '@formily/core';
import {createSchemaField} from '@formily/react';
import {Form, FormItem, Input, Password, Submit} from '@formily/antd';
import {Card} from 'antd';
import * as ICONS from '@ant-design/icons';
import {FormProps} from '@formily/antd/esm/form';
import {useRvModal} from '@/components/common/RvModal';
import axios from 'axios';
import {Result} from '@/types/result';
import RvRequest from '@/utils/rvRequest';

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
    const rvModal = useRvModal();
    const [loading, setLoading] = useState<boolean>(false);
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
                        setLoading(true);
                        const result: Result = await RvRequest.do(() => axios.post('/app/verifyInitKey', data));
                        setLoading(false);
                        if (result.successful) {
                            rvModal.success({
                                content: '密钥验证成功！',
                                onOk() {
                                    props.onPass?.();
                                }
                            });
                        } else {
                            rvModal.error({
                                content: '密钥验证失败！请检查密钥是否已过期。',
                                onOk() {
                                    props.onReject?.();
                                }
                            });
                        }
                    }}
                >
                    <SchemaField schema={schema}/>
                    <Submit block size="large" loading={loading}>
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
