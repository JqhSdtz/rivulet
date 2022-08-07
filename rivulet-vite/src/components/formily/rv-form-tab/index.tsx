import {FormTab} from '@formily/antd';
import React, {useState} from 'react';
import {Button, TabsProps} from 'antd';
import {useParentForm} from '@formily/react';
import axios from 'axios';

const Submit = () => {
    const form = useParentForm();
    let [isLoading, setLoading] = useState(false);
    const onClick = () => {
        form.validate().then(() => {
            setLoading(true);
            form.submit((data) => axios.post('/data_model', data))
                .then(() => setLoading(false));
        }).catch(() => {
        });
    };
    return <Button type="primary" onClick={onClick} loading={isLoading}>提交</Button>;
};

export const RvFormTab: React.FC<TabsProps> = (props) => {
    const tabProps = {...props};
    tabProps.tabBarExtraContent = <Submit/>;
    return <FormTab {...tabProps}/>;
};
