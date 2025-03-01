package liquibase.ext.hibernate.snapshot;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.structure.DatabaseObject;
import org.hibernate.MappingException;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Base class for all Hibernate SnapshotGenerators
 */
public abstract class HibernateSnapshotGenerator implements SnapshotGenerator {

    private static final int PRIORITY_HIBERNATE_ADDITIONAL = 200;
    private static final int PRIORITY_HIBERNATE_DEFAULT = 100;

    private Class<? extends DatabaseObject> defaultFor = null;
    private Class<? extends DatabaseObject>[] addsTo = null;


    // !!!为了获取字段上的注解，提前保存字段和Field对象的映射关系
    private static final String ColumnFieldMapKey = "ColumnFieldMap";
    // !!!为了方便设置表的注释，提前保存表名和实体类的映射关系
    private static final String TableClassMapKey = "TableClassMap";
    // !!!为了保存Liquibase的Table对象，用于在构造外键Snapshot时获取对应的Table
    private static final String TableMapKey = "TableMap";

    protected HibernateSnapshotGenerator(Class<? extends DatabaseObject> defaultFor) {
        this.defaultFor = defaultFor;
    }

    protected HibernateSnapshotGenerator(Class<? extends DatabaseObject> defaultFor, Class<? extends DatabaseObject>[] addsTo) {
        this.defaultFor = defaultFor;
        this.addsTo = addsTo;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return null;
    }

    @Override
    public final int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof HibernateDatabase) {
            if (defaultFor != null && defaultFor.isAssignableFrom(objectType)) {
                return PRIORITY_HIBERNATE_DEFAULT;
            }
            if (addsTo() != null) {
                for (Class<? extends DatabaseObject> type : addsTo()) {
                    if (type.isAssignableFrom(objectType)) {
                        return PRIORITY_HIBERNATE_ADDITIONAL;
                    }
                }
            }
        }
        return PRIORITY_NONE;

    }

    @Override
    public final Class<? extends DatabaseObject>[] addsTo() {
        return addsTo;
    }

    @Override
    public final DatabaseObject snapshot(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        if (defaultFor != null && defaultFor.isAssignableFrom(example.getClass())) {
            DatabaseObject result = snapshotObject(example, snapshot);
            return result;
        }

        // !!!在Metadata中设置保存对应类、对应字段和对应LiquibaseTable的Map
        snapshot.getMetadata().computeIfAbsent(ColumnFieldMapKey, k -> new HashMap<>());
        snapshot.getMetadata().computeIfAbsent(TableClassMapKey, k -> new HashMap<>());
        snapshot.getMetadata().computeIfAbsent(TableMapKey, k -> new HashMap<>());

        DatabaseObject chainResponse = chain.snapshot(example, snapshot);
        if (chainResponse == null) {
            return null;
        }
        if (addsTo() != null) {
            for (Class<? extends DatabaseObject> addType : addsTo()) {
                if (addType.isAssignableFrom(example.getClass())) {
                    if (chainResponse != null) {
                        addTo(chainResponse, snapshot);
                    }
                }
            }
        }
        return chainResponse;

    }

    protected abstract DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException;

    protected abstract void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException;

    protected org.hibernate.mapping.Table findHibernateTable(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        HibernateDatabase database = (HibernateDatabase) snapshot.getDatabase();
        // MetadataImplementor metadata = (MetadataImplementor) database.getMetadata();

        // !!!此处有修改和增加，为了获取字段的注解
        MetadataImpl metadata = (MetadataImpl) database.getMetadata();
        Map<String, PersistentClass> entityBindingMap = metadata.getEntityBindingMap();
        entityBindingMap.values().forEach(entity -> {
            Class entityClass = entity.getMappedClass();
            if (entityClass == null) return;
            ((Map<String, Class>) snapshot.getMetadata().get(TableClassMapKey)).put(entity.getTable().getName(), entityClass);
            for (Field field: entityClass.getDeclaredFields()) {
                Property property;
                try {
                    property = entity.getProperty(field.getName());;
                } catch (MappingException exception) {
                    continue;
                }
                String tableName = entity.getTable().getName();
                // !!!这里原来是用的Iterator，但是新版本的hibernate中无法获取Iterator，所以直接获取List
                List<Column> columnList = property.getValue().getColumns();
                if (columnList.isEmpty()) continue;
                String columnName = columnList.get(0).getText();
                ((Map<String, Field>) snapshot.getMetadata().get(ColumnFieldMapKey)).put(tableName + "." + columnName, field);
            }
        });

        Collection<Table> tmapp = metadata.collectTableMappings();

        for (Table hibernateTable : tmapp) {
            if (hibernateTable.getName().equalsIgnoreCase(example.getName())) {
                return hibernateTable;
            }
        }
        return null;
    }

    // !!!增加获取ColumnField的方法
    protected Field getColumnField(DatabaseSnapshot snapshot, String tableName, String columnName) {
        return  ((Map<String, Field>) snapshot.getMetadata().get(ColumnFieldMapKey)).get(tableName + "." + columnName);
    }

    // !!!增加获取TableClass的方法
    protected Class getTableClass(DatabaseSnapshot snapshot, String tableName) {
        return ((Map<String, Class>) snapshot.getMetadata().get(TableClassMapKey)).get(tableName);
    }

    // !!!增加设置LiquibaseTable的方法
    protected void setLiquibaseTable(DatabaseSnapshot snapshot, String tableName, liquibase.structure.core.Table table) {
        ((Map<String, liquibase.structure.core.Table>) snapshot.getMetadata().get(TableMapKey)).put(tableName, table);
    }

    // !!!增加获取LiquibaseTable的方法
    protected liquibase.structure.core.Table getLiquibaseTable(DatabaseSnapshot snapshot, String tableName) {
        return ((Map<String, liquibase.structure.core.Table>) snapshot.getMetadata().get(TableMapKey)).get(tableName);
    }
}
