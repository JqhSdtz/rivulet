function getModelPropsSchema() {
    const inputs = {
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
        }
    };

    const grid = {
        type: 'void',
        'x-component': 'FormGrid',
        'x-component-props': {
            // 有了固定宽度或最小宽度才会自动调整列数量
            minWidth: 175,
            minColumns: 1,
            maxColumns: 3
        },
        properties: inputs
    };

    const layout = {
        type: 'void',
        'x-component': 'FormLayout',
        'x-component-props': {
            labelWrap: true
        },
        properties: {
            grid
        }
    };

    return {
        type: 'void',
        'x-component': 'FormTab.TabPane',
        'x-component-props': {
            tab: '属性'
        },
        properties: {
            layout
        }
    };
}

function getFieldsTabSchema() {
    const inputs = {
        sort: {
            type: 'void',
            'x-decorator': 'FormItem',
            'x-component': 'ArrayItems.SortHandle'
        },
        name: {
            type: 'string',
            title: '名称',
            'x-decorator': 'FormItem',
            required: true,
            'x-component': 'Input'
        },
        code: {
            type: 'string',
            title: '编码(FieldCode)',
            'x-decorator': 'FormItem',
            required: true,
            'x-component': 'Input'
        },
        dataType: {
            type: 'string',
            title: '数据类型',
            'x-decorator': 'FormItem',
            required: true,
            'x-component': 'Input'
        },
        remove: {
            type: 'void',
            'x-decorator': 'FormItem',
            'x-component': 'ArrayItems.Remove'
        }
    };

    const array = {
        type: 'array',
        'x-component': 'ArrayItems',
        'x-decorator': 'FormItem',
        items: {
            type: 'object',
            properties: {
                space: {
                    type: 'void',
                    'x-component': 'Space',
                    properties: inputs
                }
            }
        },
        properties: {
            add: {
                type: 'void',
                title: '添加条目',
                'x-component': 'ArrayItems.Addition'
            }
        }
    };

    return {
        type: 'void',
        'x-component': 'FormTab.TabPane',
        'x-component-props': {
            tab: '字段'
        },
        properties: {
            fields: array
        }
    };
}

function getResult() {
    const entityManager = jsGlobal.getEntityManager();
    const objectMapper = jsGlobal.getObjectMapper();
    const request = jsGlobal.getRequest();
    const prototypeId = request.getParameter('id');
    let prototype = {};
    if (prototypeId !== null) {
        const query = entityManager.createQuery('select p from RvPrototype p where p.id = :id')
            .setParameter('id', prototypeId);
        prototype = query.getSingleResult();
    }
    return JSON.stringify({
        schema: {
            type: 'object',
            properties: {
                collapse: {
                    type: 'void',
                    'x-component': 'RvFormTab',
                    'x-component-props': {
                        style: {
                            margin: '5px 20px'
                        },
                        formTab: '{{formTab}}'
                    },
                    properties: {
                        modelPropsSchema: getModelPropsSchema(),
                        fieldsTabSchema: getFieldsTabSchema()
                    }
                }
            }
        },
        values: JSON.parse(objectMapper.writeValueAsString(prototype))
    });
}

getResult();
