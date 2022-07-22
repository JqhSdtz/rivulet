import {FormGrid, FormItem, FormLayout, FormTab, Input} from '@formily/antd';
import {createSchemaField, FormProvider} from '@formily/react';
import {createForm} from '@formily/core';
import axios from 'axios';
import {useMount} from 'ahooks';

const SchemaField = createSchemaField({
    components: {
        FormItem,
        FormTab,
        Input,
        FormGrid,
        FormLayout
    }
});

const form = createForm();
const formTab = FormTab.createFormTab();

const propertiesTabSchema = {
    type: 'void',
    'x-component': 'FormTab.TabPane',
    'x-component-props': {
        tab: '属性'
    },
    properties: {
        layout: {
            type: 'void',
            'x-component': 'FormLayout',
            'x-component-props': {
                labelWrap: true
            },
            properties: {
                grid: {
                    type: 'void',
                    'x-component': 'FormGrid',
                    'x-component-props': {
                        // 有了固定宽度或最小宽度才会自动调整列数量
                        minWidth: 175,
                        minColumns: 1,
                        maxColumns: 3
                    },
                    properties: {
                        name: {
                            type: 'string',
                            title: '名称',
                            required: true,
                            'x-decorator': 'FormItem',
                            'x-component': 'Input'
                        },
                        title: {
                            type: 'string',
                            title: '标题',
                            required: true,
                            'x-decorator': 'FormItem',
                            'x-component': 'Input'
                        },
                        field3: {
                            type: 'string',
                            title: '字段3',
                            'x-decorator': 'FormItem',
                            'x-component': 'Input'
                        },
                        field4: {
                            type: 'string',
                            title: '字段4',
                            'x-decorator': 'FormItem',
                            'x-component': 'Input'
                        },
                        field5: {
                            type: 'string',
                            title: '字段5',
                            'x-decorator': 'FormItem',
                            'x-component': 'Input'
                        },
                        field6: {
                            type: 'string',
                            title: '字段6',
                            'x-decorator': 'FormItem',
                            'x-component': 'Input'
                        }
                    }
                }
            }
        }
    }
};

const schema = {
    type: 'object',
    properties: {
        collapse: {
            type: 'void',
            'x-component': 'FormTab',
            'x-component-props': {
                style: {
                    margin: '5px 20px'
                },
                formTab: '{{formTab}}'
            },
            properties: {
                propertiesTab: propertiesTabSchema,
                tab2: {
                    type: 'void',
                    'x-component': 'FormTab.TabPane',
                    'x-component-props': {
                        tab: 'A2'
                    },
                    properties: {
                        bbb: {
                            type: 'string',
                            title: 'BBB',
                            'x-decorator': 'FormItem',
                            required: true,
                            'x-component': 'Input'
                        },
                        ccc: {
                            type: 'string',
                            title: 'CCC',
                            'x-decorator': 'FormItem',
                            required: true,
                            'x-component': 'Input'
                        }
                    }
                },
                tab3: {
                    type: 'void',
                    'x-component': 'FormTab.TabPane',
                    'x-component-props': {
                        tab: 'A3'
                    },
                    properties: {
                        ccc: {
                            type: 'string',
                            title: 'CCC',
                            'x-decorator': 'FormItem',
                            required: true,
                            'x-component': 'Input'
                        }
                    }
                }
            }
        }
    }
};

export default () => {
    useMount(() => {
        axios.get('/data_model/form_schema').then(console.log);
    });
    return (
        <FormProvider form={form}>
            <SchemaField schema={schema} scope={{formTab}}/>
        </FormProvider>
    );
}
