import {useRequest} from 'ahooks';
import axios from 'axios';
import {RecursionField, useFieldSchema, useForm} from '@formily/react';
import React, {useEffect, useState} from 'react';
import {filterEmptyString} from '@/utils/formilyUtil';
import {Result} from '@/types/result';
import RvRequest from '@/utils/rvRequest';
import {useRvModal} from '@/components/common/RvModal';
import {useFormInstance} from "@/components/formily/hooks";
import {act} from 'react-dom/test-utils';

interface RvTableProps {
    baseUrl: string;
    requestType?: string;
}

export interface PaginationType {
    pageNumber: number;
    pageSize: number;
    totalNumber?: number;
    totalPages?: number;
}

export const getInitPagination = () => ({
    pageSize: 10,
    pageNumber: 0
});

export interface RvTableContextType {
    isDataSourcePageable: boolean;
    baseUrl: string;
    loading: boolean;
    query: (queryPagination?: PaginationType) => Promise<any>;
    pagination: PaginationType;
    setPagination: (pagination: PaginationType) => void;
}

export const RvTableContext = React.createContext({} as RvTableContextType);

export const RvTableComp: React.FC<RvTableProps> = (props) => {
    const rvModal = useRvModal();
    const initPagination = getInitPagination();
    const initParams = {
        pagination: initPagination
    };
    const queryRequest = (params) => {
        const data = params ?? initParams;
        if (props.requestType === 'js') {
            return RvRequest.runJsServiceRaw(props.baseUrl, data);
        } else {
            return RvRequest.doPostRaw(props.baseUrl, data);
        }
    };
    const [pagination, setPagination] = useState<PaginationType>(initPagination);
    const {data, loading, runAsync} = useRequest<any, any[]>(queryRequest);
    const result = data?.data as Result;
    const form = useFormInstance();
    const query = (queryPagination) => {
        const params = {
            payload: form.getValuesIn('toolbar'),
            pagination: queryPagination ?? pagination
        };
        return runAsync(filterEmptyString(params));
    };
    useEffect(() => {
        if (!result) return;
        if (!result.successful) {
            rvModal.result(result);
        } else {
            const payload = result.payload;
            form.setValues({
                table: payload.content
            });
            pagination.totalNumber = payload.totalElements;
            setPagination(pagination);
        }
    }, [result]);
    const rvTableSchema = useFieldSchema();
    const rvTableContextValue = {
        isDataSourcePageable: true,
        baseUrl: props.baseUrl,
        query,
        loading,
        pagination,
        setPagination
    };
    return (
        <RvTableContext.Provider value={rvTableContextValue}>
            <RecursionField schema={rvTableSchema.properties.toolbar}/>
            <RecursionField schema={rvTableSchema.properties.table}/>
        </RvTableContext.Provider>
    );
};
