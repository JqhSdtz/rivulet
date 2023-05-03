import {FormTab} from '@formily/antd';
import {createSchemaField, FormProvider, useField} from '@formily/react';
import axios from 'axios';
import {useMount, useRequest} from 'ahooks';
import {PageLoading} from '@/layouts/BasicLayout';
import {allComponents} from '@/utils/formilyUtil';
import {useFormInstance} from '@/components/formily/hooks';
import useUrlState from '@ahooksjs/use-url-state';
import RvRequest from '@/utils/rvRequest';
import {GeneralField} from '@formily/core';
import {IFieldProps} from '@formily/core/esm/types';

const SchemaField = createSchemaField({
    components: allComponents as any,
    scope: {
        getColumnOptions: (field: GeneralField & IFieldProps) => {
            const columns = field.query('columns').value();
            field.dataSource = columns.map(column => ({
                label: column.title,
                value: column.id
            }));
        },
        findColumn: (form, id) => {
            const columns = form.query('columns').value();
            return columns.find(column => column.id === id);
        }
    }
});

const formTab = FormTab.createFormTab();

const FormComponent = (props) => {
    const form = useFormInstance();
    useMount(() => {
        form.setValues(props.values);
    });
    return (
        <FormProvider form={form}>
            <SchemaField schema={props.schema} scope={{formTab}}/>
        </FormProvider>
    );
};

export default () => {
    const [urlState] = useUrlState();
    const {data, loading} = useRequest(() => RvRequest.doRaw(() => axios.get('/dataModel/ModelDetailSchema', {
        params: urlState
    })));
    return loading ? <PageLoading/> : <FormComponent {...JSON.parse(data.data.payload)}/>;
}
