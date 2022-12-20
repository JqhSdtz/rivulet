import {FormTab} from '@formily/antd';
import React, {useState} from 'react';
import {Button, TabsProps} from 'antd';
import {RecursionField, Schema, useParentForm} from '@formily/react';
import axios from 'axios';
import RvRequest from '@/utils/rvRequest';
import {useRvModal} from '@/components/common/RvModal';

const Submit = () => {
    const form = useParentForm();
    const rvModal = useRvModal();
    let [isLoading, setLoading] = useState(false);
    const onClick = () => {
        rvModal.success({content: 'test'});
        form.validate().then(() => {
            setLoading(true);
            // form.submit((data) => RvRequest.do(() => axios.post('/dataModel/create', data)))
            form.submit((data) => console.log(data))
                .then(() => setLoading(false));
        }).catch(() => {
        });
    };
    return <Button type="primary" onClick={onClick} loading={isLoading}>提交</Button>;
};

export const RvFormTab: React.FC<TabsProps> = (props) => {
    const tabProps = {...props};
    tabProps.tabBarExtraContent = <Submit/>;
    tabProps.items.forEach(item => {
        item.children = <RecursionField schema={item.children as Schema}/>;
    });
    return <FormTab {...tabProps}/>;
};
