import * as AntdComponents from '@formily/antd';
import * as RvComponents from '@/components/formily';
import * as AntdIcons from '@ant-design/icons';

import {Schema} from '@formily/react';

export const allComponents = {
    ...AntdComponents,
    ...RvComponents,
    ...AntdIcons
};

export const wrapObject: (propName: string, schema: any) => Schema =
    (propName, schema) => {
        return {
            type: 'object',
            properties: {
                [propName]: schema
            }
        } as Schema;
    };

export const doubleWrapObject: (propName: string, schema: any) => Schema =
    (propName, schema) => {
        return wrapObject(propName, wrapObject(propName, schema));
    };


export const filterEmptyString = (data) => {
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
}
