package org.laputa.rivulet.common.util;

import cn.hutool.core.lang.UUID;
import jakarta.persistence.Table;
import liquibase.structure.core.DataType;
import liquibase.util.StringUtil;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseUtil {
    private final static Pattern pattern = Pattern.compile("([^\\(]*)\\s*\\(?\\s*(\\d*)?\\s*,?\\s*(\\d*)?\\s*([^\\(]*?)\\)?");

    @SneakyThrows
    public static boolean isTableExist(DataSource dataSource, String tableName) {
        Connection connection = dataSource.getConnection();
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet resultSet = metadata.getTables(null ,null, null, null);
        return resultSet.next() && resultSet.getString("TABLE_NAME").equalsIgnoreCase(tableName);
    }

    public static String getTableNameByClass(Class<?> clazz) {
        Table annotation = clazz.getAnnotation(Table.class);
        if (annotation != null) {
            return annotation.name();
        }
        return null;
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    public static DataType toDataType(String rvColumnDataType, Integer sqlTypeCode) {
        Matcher matcher = pattern.matcher(rvColumnDataType);
        if (!matcher.matches()) {
            return null;
        }
        DataType dataType = new DataType(matcher.group(1));
        if (matcher.group(3).isEmpty()) {
            if (!matcher.group(2).isEmpty()) {
                dataType.setColumnSize(Integer.parseInt(matcher.group(2)));
            }
        } else {
            dataType.setColumnSize(Integer.parseInt(matcher.group(2)));
            dataType.setDecimalDigits(Integer.parseInt(matcher.group(3)));
        }
        String extra = StringUtil.trimToNull(matcher.group(4));
        // !!!添加默认的size unit，防止类型比对的时候出现错误
        dataType.setColumnSizeUnit(DataType.ColumnSizeUnit.BYTE);
        if (extra != null) {
            if (extra.equalsIgnoreCase("char")) {
                dataType.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
            }
        }
        dataType.setDataTypeId(sqlTypeCode);
        return dataType;
    }
}
