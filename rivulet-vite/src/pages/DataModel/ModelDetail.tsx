import {FormTab} from '@formily/antd';
import {createSchemaField, FormProvider} from '@formily/react';
import {createForm} from '@formily/core';
import axios from 'axios';
import {useRequest} from 'ahooks';
import {PageLoading} from '@/layouts/BasicLayout';
import {antdComponents} from '@/utils/formilyUtil';

const SchemaField = createSchemaField({
    components: antdComponents
});

const form = createForm();
const formTab = FormTab.createFormTab();

export default () => {
    const {data, loading} = useRequest(() => axios.get('/data_model/form_schema'));
    if (loading) {
        return <PageLoading/>;
    }
    const schema = JSON.parse(data.data.payload);
    return (
        <FormProvider form={form}>
            <SchemaField schema={schema} scope={{formTab}}/>
        </FormProvider>
    );
}
