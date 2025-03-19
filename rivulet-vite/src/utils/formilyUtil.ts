import * as AntdComponents from '@formily/antd';
import * as RvComponents from '@/components/formily';
import * as AntdIcons from '@ant-design/icons';
import {Field, FormPath, registerValidateRules} from '@formily/core';
import {isEmpty, isArr, isValid, toArr, isEqual} from '@formily/shared';
import rvUtil from "@/utils/rvUtil";
import rvRequest from "@/utils/rvRequest";

export const allComponents = {
    ...AntdComponents,
    ...RvComponents,
    ...AntdIcons
};

export const filterEmptyString = data => {
    if (typeof data === 'object') {
        if (data.constructor === Object) {
            const cloned = {};
            for (let key in data) {
                const filtered = filterEmptyString(data[key]);
                if (typeof filtered !== 'undefined') {
                    cloned[key] = filtered;
                }
            }
            return cloned;
        } else if (data.constructor === Array) {
            return data.map(filterEmptyString);
        }
    } else {
        if (data !== '') return data;
    }
};

const isValidateEmpty = (value: any) => {
    if (isArr(value)) {
        for (let i = 0; i < value.length; i++) {
            if (isValid(value[i])) return false;
        }
        return true;
    } else {
        //compat to draft-js
        if (value?.getCurrentContent) {
            /* istanbul ignore next */
            return !value.getCurrentContent()?.hasText();
        }
        return isEmpty(value);
    }
};
export const registerRvValidateRules = () => {
    registerValidateRules({
        unique(value, rule, ctx) {
            if (isValidateEmpty(value) || !rule.unique) return '';
            const field: Field = ctx.field;
            const list = field.path.pop().pop().getIn(field.form.values);
            value = toArr(list);
            return value.some((item: any, index: number) => {
                for (let i = 0; i < value.length; i++) {
                    if (i !== index && isEqual(value[i][rule.uniqueField], item[rule.uniqueField])) {
                        return false;
                    }
                }
                return true;
            })
                ? ''
                : rule.message;
        }
    });
};
export const getRvScope = (injected: any) => ({
    ...injected,
    rvUtil,
    rvRequest
});