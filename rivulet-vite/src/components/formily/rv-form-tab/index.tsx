import {FormTab} from '@formily/antd';
import React, {useState} from 'react';
import {Button, TabsProps} from 'antd';
import {markRaw, model} from '@formily/reactive';
import {useParentForm} from '@formily/react';
import axios from 'axios';
import RvRequest from '@/utils/rvRequest';
import {useRvModal} from '@/components/common/RvModal';
import {useCreation} from 'ahooks';

const Submit = () => {
    const form = useParentForm();
    const rvModal = useRvModal();
    let [isLoading, setLoading] = useState(false);
    const onClick = () => {
        form.validate().then(() => {
            setLoading(true);
            form.submit((data) => RvRequest.do(() => axios.post('/dataModel/create', data)))
                .then(() => setLoading(false)).then(() => rvModal.success({content: '保存成功'}));
        }).catch(() => {
            rvModal.error({content: '未知错误'});
        });
    };
    return <Button type="primary" onClick={onClick} loading={isLoading}>提交</Button>;
};

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

export const RvFormTab: React.FC<TabsProps & { formTab: any }> = (props) => {
    const tabProps = {...props};
    // 手动设置formTab，防止所有formTab共用一个实例，导致无法在不同的标签页下打开不同的tab
    tabProps.formTab = useCreation(() => createFormTab(), []);
    tabProps.tabBarExtraContent = <Submit/>;
    // 必须用TabPane组件，否则会破坏formily的formTab结构
    // tabProps.items.forEach(item => {
    //     item.children = <RecursionField schema={item.children as Schema}/>;
    // });
    return <FormTab {...tabProps}/>;
};
