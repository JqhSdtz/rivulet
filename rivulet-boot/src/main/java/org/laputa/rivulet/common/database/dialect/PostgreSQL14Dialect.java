package org.laputa.rivulet.common.database.dialect;

import org.hibernate.dialect.PostgreSQLDialect;

import java.sql.Types;

/**
 * @author JQH
 * @since 下午 10:50 22/10/15
 */
public class PostgreSQL14Dialect extends PostgreSQLDialect {
    public PostgreSQL14Dialect() {
        super();
//      postgreSql14的Boolean对应的是bool类型，原来的Dialect都不对，会导致liquibase对不上
//      从而一直算作修改的字段
//        registerColumnType(Types.BOOLEAN, "bool");
    }
}
