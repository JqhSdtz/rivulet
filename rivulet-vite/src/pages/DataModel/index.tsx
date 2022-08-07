import React from 'react';
import {createForm} from '@formily/core';
import {createSchemaField, FormProvider} from '@formily/react';
import {antdComponents} from '@/utils/formilyUtil';

const SchemaField = createSchemaField({
    components: antdComponents
});

const form = createForm();
const schema = {
    type: 'object',
    properties: {
        toolbar: {
            type: 'void',
            'x-component': 'FormGrid',
            'x-component-props': {
                // 有了固定宽度或最小宽度才会自动调整列数量
                minWidth: 175,
                minColumns: 1,
                maxColumns: 3,
                style: {
                    margin: '1rem'
                }
            },
            properties: {
                name: {
                    type: 'string',
                    title: '名称',
                    required: true,
                    'x-decorator': 'FormItem',
                    'x-component': 'Input'
                },
                code: {
                    type: 'string',
                    title: '编码',
                    required: true,
                    'x-decorator': 'FormItem',
                    'x-component': 'Input'
                },
                description: {
                    type: 'string',
                    title: '描述',
                    'x-decorator': 'FormItem',
                    'x-component': 'Input'
                },
                addButton: {
                    type: 'void',
                    'x-component': 'RvLinkButton',
                    'x-component-props': {
                        to: './detail'
                    }
                }
            }
        },
        table: {
            type: 'array',
            'x-decorator': 'FormItem',
            'x-component': 'RvTable',
            'x-component-props': {
                pagination: {pageSize: 10},
                scroll: {x: '100%'}
            },
            items: {
                type: 'object',
                properties: {
                    column1: {
                        type: 'void',
                        'x-component': 'ArrayTable.Column',
                        'x-component-props': {width: 50, title: 'Sort', align: 'center'},
                        properties: {
                            sort: {
                                type: 'void',
                                'x-component': 'ArrayTable.SortHandle'
                            }
                        }
                    },
                    column2: {
                        type: 'void',
                        'x-component': 'ArrayTable.Column',
                        'x-component-props': {width: 80, title: 'Index', align: 'center'},
                        properties: {
                            index: {
                                type: 'void',
                                'x-component': 'ArrayTable.Index'
                            }
                        }
                    },
                    column3: {
                        type: 'void',
                        'x-component': 'ArrayTable.Column',
                        'x-component-props': {width: 200, title: 'A1'},
                        properties: {
                            a1: {
                                type: 'string',
                                'x-decorator': 'Editable',
                                'x-component': 'Input'
                            }
                        }
                    },
                    column4: {
                        type: 'void',
                        'x-component': 'ArrayTable.Column',
                        'x-component-props': {width: 200, title: 'A2'},
                        properties: {
                            a2: {
                                type: 'string',
                                'x-decorator': 'FormItem',
                                'x-component': 'Input'
                            }
                        }
                    },
                    column5: {
                        type: 'void',
                        'x-component': 'ArrayTable.Column',
                        'x-component-props': {width: 200, title: 'A3'},
                        properties: {
                            a3: {
                                type: 'string',
                                'x-decorator': 'FormItem',
                                'x-component': 'Input'
                            }
                        }
                    },
                    column6: {
                        type: 'void',
                        'x-component': 'ArrayTable.Column',
                        'x-component-props': {
                            title: 'Operations',
                            dataIndex: 'operations',
                            width: 200,
                            fixed: 'right'
                        },
                        properties: {
                            item: {
                                type: 'void',
                                'x-component': 'FormItem',
                                properties: {
                                    remove: {
                                        type: 'void',
                                        'x-component': 'ArrayTable.Remove'
                                    },
                                    moveDown: {
                                        type: 'void',
                                        'x-component': 'ArrayTable.MoveDown'
                                    },
                                    moveUp: {
                                        type: 'void',
                                        'x-component': 'ArrayTable.MoveUp'
                                    }
                                }
                            }
                        }
                    }
                }
            },
            properties: {
                add: {
                    type: 'void',
                    'x-component': 'ArrayTable.Addition',
                    title: '添加条目'
                }
            }
        }
    }
};

export default () => {
    return (
        <FormProvider form={form}>
            <SchemaField schema={schema}/>
            <antdComponents.FormButtonGroup>
                <antdComponents.Submit onSubmit={console.log}>提交</antdComponents.Submit>
            </antdComponents.FormButtonGroup>
        </FormProvider>
    );
}
