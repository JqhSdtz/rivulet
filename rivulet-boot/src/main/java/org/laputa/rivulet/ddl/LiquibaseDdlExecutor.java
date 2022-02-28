package org.laputa.rivulet.ddl;

import cn.hutool.core.lang.UUID;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;

/**
 * @author JQH
 * @since 下午 8:14 22/02/08
 */
public class LiquibaseDdlExecutor {
    private Liquibase liquibase;

    public LiquibaseDdlExecutor() {
        String url = "jdbc:mysql://localhost:3306/rivulet?useSSL=false&characterEncoding=utf8&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String username = "rivulet_dev";
        String password = "9iULadbEOudrLimc2FyXjGM1acmAH9ZH";
        Database database = null;
        try {
            database = DatabaseFactory.getInstance().openDatabase(url, username, password, null, null);
            DatabaseChangeLog changeLog = new DatabaseChangeLog();
            this.liquibase = new Liquibase(changeLog, null, database);
            this.addTable();
            this.doUpdate();
        } catch (DatabaseException e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                try {
                    database.close();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addTable() {
        try {
            DatabaseChangeLog changeLog = this.liquibase.getDatabaseChangeLog();
            changeLog.setChangeLogId(UUID.randomUUID().toString(true));
            ChangeSet changeSet = new ChangeSet(changeLog);
            changeLog.addChangeSet(changeSet);
            CreateTableChange createTableChange = new CreateTableChange();
            createTableChange.setTableName("rv_test0");
            ColumnConfig idColumn = new ColumnConfig();
            idColumn.setName("id").setType("varchar(64)");
            createTableChange.addColumn(idColumn);
            changeSet.addChange(createTableChange);
        } catch (LiquibaseException e) {
            e.printStackTrace();
        }
    }

    public void doUpdate() {
        try {
            this.liquibase.update((Contexts) null);
        } catch (LiquibaseException e) {
            e.printStackTrace();
        }
    }

}
