import {useRequest} from 'ahooks';
import axios from 'axios';
import {RecursionField, useFieldSchema, useForm} from '@formily/react';
import React, {useEffect} from 'react';
import {filterEmptyString} from '@/utils/formilyUtil';
import {Result} from '@/types/result';
import RvRequest from '@/utils/rvRequest';

interface RvTableProps {
    baseUrl: string;
}

export interface RvTableContextType {
    baseUrl: string;
    loading: boolean;
    query: () => void;
}

export const RvTableContext = React.createContext({} as RvTableContextType);

export const RvTable: React.FC<RvTableProps> = (props) => {
    const queryRequest = (params) => {
        return RvRequest.doRaw(() => axios.get(props.baseUrl, {
            params
        }));
    };
    const {data, loading, run} = useRequest<any, any[]>(queryRequest);
    const result = data?.data as any as Result;
    const form = useForm();
    useEffect(() => {
        if (!result) return;
        form.setValues({
            table: result.payload
        });
    }, [result]);
    const rvTableSchema = useFieldSchema();
    const query = () => {
        const params = form.getValuesIn('toolbar');
        run(filterEmptyString(params));
    };
    const rvTableContextValue = {
        baseUrl: props.baseUrl,
        query,
        loading
    };
    return (
        <RvTableContext.Provider value={rvTableContextValue}>
            <RecursionField schema={rvTableSchema.properties.toolbar}/>
            <RecursionField schema={rvTableSchema.properties.table}/>
        </RvTableContext.Provider>
    );
};
