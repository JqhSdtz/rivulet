package liquibase.ext.hibernate.util;

import liquibase.ext.hibernate.DatabaseObjectAttrName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexStoreUtil {

    private static Map<String, Index> getIndexStoreMap(List<Column> columns) {
        if (columns == null || columns.isEmpty()) return null;
        Table table = (Table) columns.get(0).getRelation();
        if (table == null) return null;
        Map<String, Index> indexStoreMap = table.getAttribute(DatabaseObjectAttrName.IndexStoreMap, Map.class);
        if (indexStoreMap == null) {
            indexStoreMap = new HashMap<>();
            table.setAttribute(DatabaseObjectAttrName.IndexStoreMap, indexStoreMap);
        }
        return indexStoreMap;
    }

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
        getIndexStoreMap(columns).put(key, index);
    }

    public static Index getIndex(List<Column> columns) {
        String key = getKey(columns);
        if (key == null) return null;
        return getIndexStoreMap(columns).get(key);
    }

}
