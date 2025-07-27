import {createSchemaField, FormProvider} from '@formily/react';
import {useMount, useRequest} from 'ahooks';
import {PageLoading} from '@/layouts/BasicLayout';
import {allComponents, getRvScope} from '@/utils/formilyUtil';
import {useFormInstance} from '@/components/formily/hooks';
import useUrlState from '@ahooksjs/use-url-state';
import RvRequest from '@/utils/rvRequest';
import rvRequest from '@/utils/rvRequest';
import rvUtil from '@/utils/rvUtil';

const SchemaField = createSchemaField({
    components: allComponents as any
});

const FormComponent = props => {
    const form = useFormInstance();
    useMount(() => {
        form.setValues(props.values);
    });
    return (
        <FormProvider form={form}>
            <SchemaField scope={{$rvScope: getRvScope({rvInjected: props.rvInjected})}} schema={props.schema} />
        </FormProvider>
    );
};

export default () => {
    const [urlState] = useUrlState();
    const {data, loading} = useRequest(() => RvRequest.runJsSchema('dbms_model/Detail/index.mjs', urlState));
    return loading ? <PageLoading /> : <FormComponent {...data.data.payload} />;
};
