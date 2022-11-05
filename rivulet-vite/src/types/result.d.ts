import {AxiosError, AxiosResponse} from 'axios';

interface Result {
    successful: boolean,
    payload?: any,
    returnMessage?: string,
    errorCode?: string,
    errorMessage?: string,
    rawResponse?: AxiosResponse,
    rawError?: AxiosError
}
