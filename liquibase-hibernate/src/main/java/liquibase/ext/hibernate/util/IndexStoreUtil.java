package liquibase.ext.hibernate.util;

import liquibase.structure.core.Column;
import liquibase.structure.core.Index;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexStoreUtil {
    private static final Map<String, Index> indexMap = new HashMap<>();

    private static String getKey(List<Column> columns) {
        if (columns == null || columns.isEmpty()) return null;
        String tableName = columns.get(0).getRelation().getName();
        StringBuilder stringBuilder = new StringBuilder(tableName).append(":");
        for (var column : columns) {
            stringBuilder.append(column.getName()).append(",");
        }
        return stringBuilder.toString();
    }

    public static void addIndex(List<Column> columns, final Index index) {
        String key = getKey(columns);
        if (key == null) return;
        indexMap.put(key, index);
    }

    public static Index getIndex(List<Column> columns) {
        String key = getKey(columns);
        if (key == null) return null;
        return indexMap.get(key);
    }

}
