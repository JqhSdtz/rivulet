import {FormTab} from '@formily/antd';
import React, {useState} from 'react';
import {Button, TabsProps} from 'antd';
import {markRaw, model} from '@formily/reactive';
import {RecursionField, Schema, useParentForm} from '@formily/react';
import axios from 'axios';
import RvRequest from '@/utils/rvRequest';
import {useRvModal} from '@/components/common/RvModal';
import {useCreation} from 'ahooks';

// 来自formily/antd中的form-tab组件
const createFormTab = (defaultActiveKey?: string) => {
    const formTab = model({
        activeKey: defaultActiveKey,
        setActiveKey(key: string) {
            formTab.activeKey = key;
        }
    });
    return markRaw(formTab);
};

export const RvFormTab: React.FC<TabsProps & {formTab: any; extraList: Schema[]}> = props => {
    const tabProps = {...props};
    // 手动设置formTab，防止所有formTab共用一个实例，导致无法在不同的标签页下打开不同的tab
    tabProps.formTab = useCreation(() => createFormTab(), []);
    tabProps.tabBarExtraContent = (
        <>
            {tabProps.extraList.map((extra, index) => (
                <div key={index} style={{display: 'inline-block', marginRight: '1rem'}}>
                    <RecursionField schema={extra} />
                </div>
            ))}
        </>
    );

    // 必须用TabPane组件，否则会破坏formily的formTab结构
    // tabProps.items.forEach(item => {
    //     item.children = <RecursionField schema={item.children as Schema}/>;
    // });
    return <FormTab {...tabProps} />;
};
