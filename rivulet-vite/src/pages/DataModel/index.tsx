import React from 'react';
import {createSchemaField, FormProvider} from '@formily/react';
import {
    allComponents
} from '@/utils/formilyUtil';
import {useFormInstance} from '@/components/formily/hooks';
import {useRequest} from 'ahooks';
import RvRequest from '@/utils/rvRequest';
import axios from 'axios';
import {PageLoading} from '@/layouts/BasicLayout';

const SchemaField = createSchemaField({
    components: allComponents as any
});

export default () => {
    const form = useFormInstance();
    const {data, loading} = useRequest(() => RvRequest.runJsSchema('DataModelIndex.mjs'));
    return loading ? <PageLoading/> : (
        <FormProvider form={form}>
            <SchemaField schema={JSON.parse(data.data.payload)}/>
        </FormProvider>
    );
}
