import {Steps} from 'antd';
import React from 'react';
import VerifyInitKey from '@/pages/InitApp/components/VerifyInitKey';
import CreateInitialAdmin from '@/pages/InitApp/components/CreateInitialAdmin';
import SuccessResult from '@/pages/InitApp/components/SuccessResult';

const {Step} = Steps;


export default () => {
    const [current, setCurrent] = React.useState(0);
    const next = () => {
        setCurrent(current + 1);
    };
    const steps = [
        {
            title: '验证初始密钥',
            content: <VerifyInitKey onPass={next}/>
        },
        {
            title: '创建初始帐号',
            content: <CreateInitialAdmin onPass={next}/>
        },
        {
            title: '初始化完成',
            content: <SuccessResult/>
        }
    ];
    return (
        <div style={{width: '100%', height: '100%', position: 'fixed', backgroundColor: '#eee'}}>
            <div style={{width: '80%', marginLeft: '10%', marginTop: '1.5rem'}}>
                <Steps current={current} items={steps}/>
                <div className="steps-content" style={{marginTop: '1rem'}}>{steps[current].content}</div>
            </div>
        </div>
    );
};
