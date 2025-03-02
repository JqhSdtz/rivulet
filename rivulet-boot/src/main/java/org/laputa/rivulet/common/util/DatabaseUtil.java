package org.laputa.rivulet.common.util;

import cn.hutool.core.lang.UUID;
import jakarta.persistence.Table;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class DatabaseUtil {
    @SneakyThrows
    public static boolean isTableExist(DataSource dataSource, String tableName) {
        Connection connection = dataSource.getConnection();
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet resultSet = metadata.getTables(null ,null, null, null);
        return resultSet.next() && resultSet.getString("TABLE_NAME").equalsIgnoreCase(tableName);
    }

    public static String getTableNameByClass(Class<?> clazz) {
        Annotation annotation = clazz.getAnnotation(Table.class);
        if (annotation != null) {
            Table table = (Table) annotation;
            return table.name();
        }
        return null;
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
