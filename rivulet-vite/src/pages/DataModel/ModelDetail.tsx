import {FormTab} from '@formily/antd';
import {createSchemaField, FormProvider} from '@formily/react';
import axios from 'axios';
import {useRequest} from 'ahooks';
import {PageLoading} from '@/layouts/BasicLayout';
import {allComponents} from '@/utils/formilyUtil';
import {useFormInstance} from '@/components/formily/hooks';
import {useEffect} from 'react';
import useUrlState from '@ahooksjs/use-url-state';

const SchemaField = createSchemaField({
    components: allComponents as any
});

const formTab = FormTab.createFormTab();

const FormComponent = (props) => {
    const form = useFormInstance();
    useEffect(() => {
        form.setValues(props.values);
    }, [props.values]);
    return (
        <FormProvider form={form}>
            <SchemaField schema={props.schema} scope={{formTab}}/>
        </FormProvider>
    );
};

export default () => {
    const [urlState] = useUrlState();
    const {data, loading} = useRequest(() => axios.get('/dataModel/form_schema', {
        params: urlState
    }));
    return loading ? <PageLoading/> : <FormComponent {...JSON.parse(data.data.payload)}/>;
}
