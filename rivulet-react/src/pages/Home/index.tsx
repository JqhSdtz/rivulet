import {useLocation, useRequest} from 'ice';
import {Table} from 'antd';
import styles from './index.module.css';
import {KeepAlive} from 'react-activation';
import {useMount} from 'ahooks';
import {useContext} from 'react';
import RouteContext from '@/layouts/BasicLayout/RouteContext';

function Home() {
    const {
        data,
        error,
        loading,
        request: fetchRepos
    } = useRequest({url: '/api/getRepos'});
    const {dataSource = []} = data || {};

    useMount(() => {
        (async function () {
            await fetchRepos();
        })();
    });

    return (
        <div className={styles.container}>
            <h2>Home page</h2>
            {error ? (
                <div>request error: {error.message}</div>
            ) : (
                <Table loading={loading} dataSource={dataSource} rowKey="id">
                    <Table.Column title="ID" dataIndex="id" key="id" />
                    <Table.Column title="名称" dataIndex="name" key="name" />
                    <Table.Column
                        title="描述"
                        dataIndex="description"
                        key="description"
                    />
                </Table>
            )}
        </div>
    );
}

export default () => {
    const location = useLocation();
    const {matchMenus} = useContext(RouteContext);
    return (
        <KeepAlive
            name={location.pathname}
            id={location.pathname}
            matchMenus={matchMenus}
            saveScrollPosition="screen"
        >
            <Home />
        </KeepAlive>
    );
};
