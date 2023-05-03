package org.laputa.rivulet.ddl;

import cn.hutool.core.lang.UUID;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import lombok.SneakyThrows;
import org.laputa.rivulet.module.data_model.entity.RvColumn;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author JQH
 * @since 下午 8:14 22/02/08
 */

@Component
public class LiquibaseDdlExecutor implements DisposableBean {

    private LiquibaseChangeConverter converter;
    private final Database database;

    public LiquibaseDdlExecutor(DataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            converter = new LiquibaseChangeConverter(database);
        } catch (SQLException | DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        if (database != null) {
            try {
                database.close();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }

    private ChangeSet getChangeSet(DatabaseChangeLog changeLog) {
        String uuid = UUID.randomUUID().toString(true);
        // dbmsList为空表示不限制dbms类型
        return new ChangeSet(uuid, "admin", false, false, "", null, null, changeLog);
    }

    private Liquibase getLiquibase() {
        // changeLog不要设置id，否则在update的时候会提示有id但没有配置liquibase hub api
        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        return new Liquibase(changeLog, null, this.database);
    }

    @SneakyThrows
    public Liquibase addTable(RvPrototype rvPrototype, @Nullable Liquibase liquibase) {
        if (liquibase == null) {
            liquibase = getLiquibase();
        }
        DatabaseChangeLog changeLog = liquibase.getDatabaseChangeLog();
        ChangeSet changeSet = getChangeSet(changeLog);
        changeLog.addChangeSet(changeSet);
        changeSet.addChange(converter.createTable(rvPrototype));
        changeSet.addChange(converter.addPrimaryKey(rvPrototype.getPrimaryKey()));
        rvPrototype.getIndexes().forEach(rvIndex -> changeSet.addChange(converter.createIndex(rvIndex)));
        rvPrototype.getForeignKeys().forEach(rvForeignKey -> changeSet.addChange(converter.addForeignKeyConstraint(rvForeignKey)));
        rvPrototype.getUniques().forEach(rvUnique -> changeSet.addChange(converter.addUniqueConstraint(rvUnique)));
        rvPrototype.getNotNulls().forEach(rvNotNull -> changeSet.addChange(converter.addNotNullConstraint(rvNotNull)));
        return liquibase;
    }

    @SneakyThrows
    public void doUpdate(Liquibase liquibase) {
        liquibase.update(new Contexts());
    }

}
