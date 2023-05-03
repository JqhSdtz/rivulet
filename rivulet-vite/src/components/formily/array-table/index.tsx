import React, {Fragment, useCallback, useContext, useEffect, useRef, useState} from 'react';
import {Badge, Button, Pagination, Select, Space} from 'antd';
import Table from '@/components/antd/table';
import {PaginationProps} from 'antd/lib/pagination';
import {ColumnProps, TableProps} from 'antd/lib/table';
import {SelectProps} from 'antd/lib/select';
import cls from 'classnames';
import {SortableContainer, SortableElement} from 'react-sortable-hoc';
import {ArrayField, FieldDisplayTypes, GeneralField} from '@formily/core';
import {observer, ReactFC, RecursionField, useField, useFieldSchema} from '@formily/react';
import {isArr} from '@formily/shared';
import {Schema} from '@formily/json-schema';

import {usePrefixCls} from '@formily/antd/lib/__builtins__';
import {ArrayBase, ArrayBaseMixins} from '@formily/antd';
import {getInitPagination, PaginationType, RvTableContext, RvTableContextType} from '@/components/formily';
import {useUpdate} from 'ahooks';
import {PlusOutlined} from '@ant-design/icons';

interface ObservableColumnSource {
    field: GeneralField;
    columnProps: ColumnProps<any>;
    schema: Schema;
    display: FieldDisplayTypes;
    name: string;
}

interface IArrayTablePaginationProps extends PaginationProps {
    dataSource?: any[];
    children?: (
        dataSource: any[],
        pagination: React.ReactNode
    ) => React.ReactElement;
}

interface IStatusSelectProps extends SelectProps<any> {
    pageSize?: number;
}

type ComposedArrayTable = React.FC<React.PropsWithChildren<TableProps<any>>> &
    ArrayBaseMixins & {
    Column?: React.FC<React.PropsWithChildren<ColumnProps<any>>>
}

const SortableRow = SortableElement((props: any) => <tr {...props} />);
const SortableBody = SortableContainer((props: any) => <tbody {...props} />);

const isColumnComponent = (schema: Schema) => {
    return schema['x-component']?.indexOf('Column') > -1;
};

const isOperationsComponent = (schema: Schema) => {
    return schema['x-component']?.indexOf('Operations') > -1;
};

const isAdditionComponent = (schema: Schema) => {
    return schema['x-component']?.indexOf('Addition') > -1;
};

const useArrayTableSources = () => {
    const arrayField = useField();
    const schema = useFieldSchema();
    const parseSources = (schema: Schema): ObservableColumnSource[] => {
        if (
            isColumnComponent(schema) ||
            isOperationsComponent(schema) ||
            isAdditionComponent(schema)
        ) {
            if (!schema['x-component-props']?.['dataIndex'] && !schema['name'])
                return [];
            const name = schema['x-component-props']?.['dataIndex'] || schema['name'];
            const field = arrayField.query(arrayField.address.concat(name)).take();
            const columnProps =
                field?.component?.[1] || schema['x-component-props'] || {};
            const display = field?.display || schema['x-display'];
            return [
                {
                    name,
                    display,
                    field,
                    schema,
                    columnProps
                }
            ];
        } else if (schema.properties) {
            return schema.reduceProperties((buf, schema) => {
                return buf.concat(parseSources(schema));
            }, []);
        }
    };

    const parseArrayItems = (schema: Schema['items']) => {
        if (!schema) return [];
        const sources: ObservableColumnSource[] = [];
        const items = isArr(schema) ? schema : [schema];
        return items.reduce((columns, schema) => {
            const item = parseSources(schema);
            if (item) {
                return columns.concat(item);
            }
            return columns;
        }, sources);
    };

    if (!schema) throw new Error('can not found schema object');

    return parseArrayItems(schema.items);
};

const useArrayTableColumns = (
    field: ArrayField,
    sources: ObservableColumnSource[]
): TableProps<any>['columns'] => {
    return sources.reduce((buf, {name, columnProps, schema, display}, key) => {
        if (display !== 'visible') return buf;
        if (!isColumnComponent(schema)) return buf;
        return buf.concat({
            ...columnProps,
            key,
            dataIndex: name,
            render: (value: any, record: any) => {
                const index = field?.value?.indexOf(record);
                const children = (
                    <ArrayBase.Item index={index} record={() => field?.value?.[index]}>
                        <RecursionField schema={schema} name={index} onlyRenderProperties/>
                    </ArrayBase.Item>
                );
                return children;
            }
        });
    }, []);
};

const useAddition = () => {
    const schema = useFieldSchema();
    return schema.reduceProperties((addition, schema, key) => {
        if (isAdditionComponent(schema)) {
            return <RecursionField schema={schema} name={key}/>;
        }
        return addition;
    }, null);
};

const schedulerRequest = {
    request: null
};

const StatusSelect: ReactFC<IStatusSelectProps> = observer(
    (props) => {
        const field = useField<ArrayField>();
        const prefixCls = usePrefixCls('formily-array-table');
        const errors = field.errors;
        const parseIndex = (address: string) => {
            return Number(
                address
                    .slice(address.indexOf(field.address.toString()) + 1)
                    .match(/(\d+)/)?.[1]
            );
        };
        const options = props.options?.map(({label, value}) => {
            const val = Number(value);
            const hasError = errors.some(({address}) => {
                const currentIndex = parseIndex(address);
                const startIndex = (val - 1) * props.pageSize;
                const endIndex = val * props.pageSize;
                return currentIndex >= startIndex && currentIndex <= endIndex;
            });
            return {
                label: hasError ? <Badge dot>{label}</Badge> : label,
                value
            };
        });

        const width = String(options?.length).length * 15;

        return (
            <Select
                value={props.value}
                onChange={props.onChange}
                options={options}
                virtual
                style={{
                    width: width < 60 ? 60 : width
                }}
                className={cls(`${prefixCls}-status-select`, {
                    'has-error': errors?.length
                })}
            />
        );
    },
    {
        scheduler: (update) => {
            clearTimeout(schedulerRequest.request);
            schedulerRequest.request = setTimeout(() => {
                update();
            }, 100);
        }
    }
);

const ArrayTablePagination: ReactFC<IArrayTablePaginationProps> = (props) => {
    const {
        isDataSourcePageable,
        query,
        pagination,
        setPagination
    } = useContext(RvTableContext);
    const dataSource = props.dataSource || [];
    const prefixCls = usePrefixCls('formily-array-table');
    const pageSize = pagination.pageSize || 10;
    const pageNumber = pagination.pageNumber + 1;
    const size = props.size || 'default';
    const total = (isDataSourcePageable ? pagination.totalNumber : dataSource?.length) ?? 0;
    const totalPage = Math.ceil(total / pageSize);
    const pages = Array.from(new Array(totalPage)).map((_, index) => {
        const page = index + 1;
        return {
            label: page,
            value: page
        };
    });
    const update = useUpdate();
    const handleChange = (currentPageNumber: number, currentPageSize) => {
        pagination.pageNumber = currentPageNumber - 1;
        if (typeof currentPageSize === 'number') {
            pagination.pageSize = currentPageSize;
        }
        if (isDataSourcePageable) {
            query(pagination).finally(() => setPagination(pagination));
        } else {
            setPagination(pagination);
            update();
        }
    };
    useEffect(() => {
        if (totalPage > 0 && totalPage < pageNumber) {
            handleChange(totalPage, pageSize);
        }
    }, [totalPage, pageNumber]);
    const renderTotal = total => {
        return (
            <Space>
                {`共${total}条数据`}
                {isDataSourcePageable ? null : <StatusSelect
                    value={pageNumber}
                    pageSize={pageSize}
                    onChange={handleChange}
                    options={pages}
                    notFoundContent={false}
                />}
            </Space>
        );
    };
    const renderPagination = () => {
        if (!isDataSourcePageable && totalPage <= 1) return;
        // !!这里把showSizeChange由原来的false改为了true，并增设showQuickJumper为true
        return (
            <div className={`${prefixCls}-pagination`}>
                <Space>
                    <Pagination
                        {...props}
                        pageSize={pageSize}
                        current={pageNumber}
                        total={total}
                        size={size}
                        showSizeChanger={true}
                        showQuickJumper={true}
                        showTotal={renderTotal}
                        onChange={handleChange}
                    />
                </Space>
            </div>
        );
    };
    let tableDataSource = dataSource;
    if (!isDataSourcePageable) {
        const startIndex = (pageNumber - 1) * pageSize;
        const endIndex = startIndex + pageSize - 1;
        tableDataSource = dataSource?.slice(startIndex, endIndex + 1);
    }

    return (
        <Fragment>
            {props.children?.(tableDataSource, renderPagination())}
        </Fragment>
    );
};

const RowComp = (props: any) => {
    return <SortableRow index={props['data-row-key'] || 0} {...props} />;
};

const InnerArrayTable = observer((props: TableProps<any>) => {
    const ref = useRef<HTMLDivElement>();
    const field = useField<ArrayField>();
    const prefixCls = usePrefixCls('formily-array-table');
    const dataSource = Array.isArray(field.value) ? field.value.slice() : [];
    const sources = useArrayTableSources();
    const columns = useArrayTableColumns(field, sources);
    const addition = useAddition();
    const defaultRowKey = (record: any) => {
        return dataSource.indexOf(record);
    };
    const addTdStyles = (node: HTMLElement) => {
        const helper = document.body.querySelector(`.${prefixCls}-sort-helper`);
        if (helper) {
            const tds = node.querySelectorAll('td');
            requestAnimationFrame(() => {
                helper.querySelectorAll('td').forEach((td, index) => {
                    if (tds[index]) {
                        td.style.width = getComputedStyle(tds[index]).width;
                    }
                });
            });
        }
    };
    const WrapperComp = useCallback(
        (props: any) => (
            <SortableBody
                useDragHandle
                lockAxis="y"
                helperClass={`${prefixCls}-sort-helper`}
                helperContainer={() => {
                    return ref.current?.querySelector('tbody');
                }}
                onSortStart={({node}) => {
                    addTdStyles(node as HTMLElement);
                }}
                onSortEnd={({oldIndex, newIndex}) => {
                    field.move(oldIndex, newIndex);
                }}
                {...props}
            />
        ),
        []
    );

    // !!!此处增加了从RvTableContext处获取loading
    const {loading} = useContext<RvTableContextType>(RvTableContext);
    // !!!此处增加了渲染扩展行的功能
    const additionalProps = {} as any;
    const arrayTableSchema: any = useFieldSchema();
    if (!additionalProps.expandable) additionalProps.expandable = {};
    if (arrayTableSchema.additionalProperties && !props.expandedRowRender) {
        additionalProps.expandable.expandedRowRender = (record, index) => {
            return (
                <RecursionField
                    schema={arrayTableSchema.additionalProperties}
                    basePath={`${field.address.entire}.${index}`}
                />
            );
        };
    }
    // !!!Table组件的title是函数类型，但json-schema定义的是string类型，做一下转换
    if (typeof props.title === 'string') {
        additionalProps.title = () => (
            <p style={{fontWeight: 'bold', marginBottom: 0}}>
                {props.title}
            </p>
        );
    }

    return (
        <ArrayTablePagination dataSource={dataSource}>
            {(tableDataSource, pager) => (
                <div ref={ref} className={prefixCls}>
                    <ArrayBase>
                        <Table
                            size="small"
                            bordered
                            rowKey={defaultRowKey}
                            loading={loading}
                            {...props}
                            {...additionalProps}
                            pagination={false}
                            columns={columns}
                            dataSource={tableDataSource}
                            components={{
                                body: {
                                    wrapper: WrapperComp,
                                    row: RowComp
                                }
                            }}
                        />
                        {addition}
                        <div style={{marginTop: 5, marginBottom: 5}}>{pager}</div>
                        {sources.map((column, key) => {
                            //专门用来承接对Column的状态管理
                            if (!isColumnComponent(column.schema)) return;
                            return React.createElement(RecursionField, {
                                name: column.name,
                                schema: column.schema,
                                onlyRenderSelf: true,
                                key
                            });
                        })}
                    </ArrayBase>
                </div>
            )}
        </ArrayTablePagination>
    );
});

const ArrayTableContainer = (props) => {
    const [pagination, setPagination] = useState<PaginationType>(getInitPagination());
    const field = useField<ArrayField>();
    const dataSource = Array.isArray(field.value) ? field.value : [];
    pagination.totalNumber = dataSource.length;
    pagination.totalPages = Math.ceil(pagination.totalNumber / pagination.pageSize);
    const rvTableContextValue: RvTableContextType = {
        isDataSourcePageable: false,
        baseUrl: '',
        loading: undefined,
        query: () => Promise.resolve(),
        pagination,
        setPagination
    };
    return (
        <RvTableContext.Provider value={rvTableContextValue}>
            <InnerArrayTable {...props}/>
        </RvTableContext.Provider>
    );
};

export const ArrayTable: ComposedArrayTable = (props: TableProps<any>) => {
    const rvTableContext = useContext<RvTableContextType>(RvTableContext);
    if (rvTableContext && rvTableContext.isDataSourcePageable) {
        return <InnerArrayTable {...props}/>;
    }
    return <ArrayTableContainer {...props}/>;
};

ArrayTable.displayName = 'ArrayTable';

ArrayTable.Column = () => {
    return <Fragment/>;
};

ArrayBase.mixin(ArrayTable);

ArrayBase.Addition = (props) => {
    const self = useField();
    const array = ArrayBase.useArray();
    const prefixCls = usePrefixCls('formily-array-base');
    if (!array) return null;
    if (array.field?.pattern !== 'editable' && array.field?.pattern !== 'disabled') {
        return null;
    }
    return (
        <Button
            type="dashed"
            block
            {...props}
            disabled={self?.disabled}
            className={cls(`${prefixCls}-addition`, props.className)}
            onClick={(e) => {
                if (array.props?.disabled) return;
                setTimeout(() => {
                    const defaultValue = props.defaultValue ? props.defaultValue() : {};
                    if (props.method === 'unshift') {
                        array.field?.unshift?.(defaultValue);
                        array.props?.onAdd?.(0);
                    } else {
                        array.field?.push?.(defaultValue);
                        array.props?.onAdd?.(array?.field?.value?.length - 1);
                    }
                }, 0);
                if (props.onClick) {
                    props.onClick(e);
                }
            }}
            icon={<PlusOutlined/>}
        >
            {props.title || self.title}
        </Button>
    );
};

const Addition: ArrayBaseMixins['Addition'] = (props) => {
    const array = ArrayBase.useArray();
    const {isDataSourcePageable, pagination, setPagination} = useContext(RvTableContext);
    return (
        <ArrayBase.Addition
            {...props}
            onClick={(e) => {
                if (isDataSourcePageable) {
                    console.warn('该表格数据为后端分页，不能使用Addition组件添加行');
                    return;
                }
                ++pagination.totalNumber;
                // 如果添加数据后将超过当前页，则自动切换到下一页
                const total = array?.field?.value.length || 0;
                if (total === pagination.totalPages * pagination.pageSize + 1) {
                    ++pagination.pageNumber;
                }
                setPagination(pagination);
                props.onClick?.(e);
            }}
        />
    );
};
ArrayTable.Addition = Addition;

export default ArrayTable;
