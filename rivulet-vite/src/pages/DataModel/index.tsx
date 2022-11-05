import React from 'react';
import {createSchemaField, FormProvider} from '@formily/react';
import {allComponents, doubleWrapObject, wrapObject} from '@/utils/formilyUtil';
import {useFormInstance} from '@/components/formily/hooks';

const SchemaField = createSchemaField({
    components: allComponents as any
});

const schema = {
    type: 'object',
    properties: {
        rvTable: {
            type: 'void',
            'x-component': 'RvTable',
            'x-component-props': {
                baseUrl: '/dataModel'
            },
            properties: {
                toolbar: doubleWrapObject('toolbar', {
                    type: 'void',
                    'x-component': 'FormGrid',
                    'x-component-props': {
                        maxColumns: 3,
                        minColumns: 2,
                        style: {
                            margin: '1rem'
                        }
                    },
                    properties: {
                        name: {
                            type: 'string',
                            title: '名称',
                            'x-decorator': 'FormItem',
                            'x-component': 'Input'
                        },
                        code: {
                            type: 'string',
                            title: '编码',
                            'x-decorator': 'FormItem',
                            'x-component': 'Input'
                        },
                        description: {
                            type: 'string',
                            title: '描述',
                            'x-decorator': 'FormItem',
                            'x-component': 'Input'
                        },
                        operations: {
                            type: 'void',
                            'x-component': 'FormGrid.GridColumn',
                            'x-component-props': {
                                gridSpan: -1,
                                style: {
                                    justifySelf: 'end'
                                }
                            },
                            properties: {
                                queryButton: {
                                    type: 'void',
                                    'x-component': 'RvQueryButton'
                                },
                                addButton: {
                                    type: 'void',
                                    'x-component': 'RvLinkButton',
                                    'x-component-props': {
                                        to: './detail',
                                        text: '新建',
                                        style: {
                                            marginLeft: '1rem'
                                        }
                                    }
                                }
                            }
                        }
                    }
                }),
                table: wrapObject('table', {
                    type: 'array',
                    'x-decorator': 'FormItem',
                    'x-component': 'ArrayTable',
                    'x-component-props': {
                        scroll: {
                            x: '100%'
                        }
                    },
                    items: {
                        type: 'object',
                        properties: {
                            sortHandle: {
                                type: 'void',
                                'x-component': 'ArrayTable.Column',
                                'x-component-props': {
                                    width: 50,
                                    title: '排序',
                                    align: 'center'
                                },
                                properties: {
                                    sort: {
                                        type: 'void',
                                        'x-component': 'ArrayTable.SortHandle'
                                    }
                                }
                            },
                            index: {
                                type: 'void',
                                'x-component': 'ArrayTable.Column',
                                'x-component-props': {
                                    width: 80,
                                    title: '序号',
                                    align: 'center'
                                },
                                properties: {
                                    index: {
                                        type: 'void',
                                        'x-component': 'ArrayTable.Index'
                                    }
                                }
                            },
                            name: {
                                type: 'void',
                                'x-component': 'ArrayTable.Column',
                                'x-component-props': {
                                    width: 200,
                                    title: '名称'
                                },
                                properties: {
                                    name: {
                                        type: 'string',
                                        'x-decorator': 'Editable',
                                        'x-component': 'Input'
                                    }
                                }
                            },
                            code: {
                                type: 'void',
                                'x-component': 'ArrayTable.Column',
                                'x-component-props': {
                                    width: 200,
                                    title: '编码'
                                },
                                properties: {
                                    code: {
                                        type: 'string',
                                        'x-decorator': 'FormItem',
                                        'x-component': 'Input'
                                    }
                                }
                            },
                            operations: {
                                type: 'void',
                                'x-component': 'ArrayTable.Column',
                                'x-component-props': {
                                    title: '操作',
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
                                            edit: {
                                                type: 'void',
                                                'x-component': 'RvLinkButton',
                                                'x-component-props': {
                                                    to: './detail',
                                                    data: {
                                                        id: '{{$record.id}}'
                                                    },
                                                    icon: wrapObject('icon', {
                                                        type: 'void',
                                                        'x-component': 'EditOutlined',
                                                        'x-component-props': {
                                                            className: 'ant-formily-array-base-copy',
                                                            style: {
                                                                marginLeft: '1rem'
                                                            }
                                                        }
                                                    })
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }
    }
};

export default () => {
    const form = useFormInstance();
    return (
        <FormProvider form={form}>
            <SchemaField schema={schema}/>
        </FormProvider>
    );
}
