import {FormTab} from '@formily/antd';
import {createSchemaField, FormProvider} from '@formily/react';
import {useMount, useRequest} from 'ahooks';
import {PageLoading} from '@/layouts/BasicLayout';
import {allComponents} from '@/utils/formilyUtil';
import {useFormInstance} from '@/components/formily/hooks';
import useUrlState from '@ahooksjs/use-url-state';
import RvRequest from '@/utils/rvRequest';

const SchemaField = createSchemaField({
    components: allComponents as any
});

const formTab = FormTab.createFormTab();

const FormComponent = props => {
    const form = useFormInstance();
    useMount(() => {
        form.setValues(props.values);
    });
    return (
        <FormProvider form={form}>
            <SchemaField schema={props.schema} scope={{formTab}} />
        </FormProvider>
    );
};

export default () => {
    const [urlState] = useUrlState();
    const {data, loading} = useRequest(() => RvRequest.runJsSchema('ModelDetail/index.mjs', urlState));
    return loading ? <PageLoading /> : <FormComponent {...data.data.payload} />;
};
