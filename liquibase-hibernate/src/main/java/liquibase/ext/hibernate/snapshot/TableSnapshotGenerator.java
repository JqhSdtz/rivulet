package liquibase.ext.hibernate.snapshot;

import liquibase.Scope;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.GlobalSetting;
import liquibase.ext.hibernate.DatabaseObjectAttrName;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.ext.hibernate.snapshot.extension.ExtendedSnapshotGenerator;
import liquibase.ext.hibernate.snapshot.extension.TableGeneratorSnapshotGenerator;
import liquibase.ext.hibernate.util.TableRemarkMetaInfo;
import liquibase.ext.hibernate.util.TableRemarkMetaInfoUtil;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.generator.Generator;
import org.hibernate.mapping.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TableSnapshotGenerator extends HibernateSnapshotGenerator {

    private List<ExtendedSnapshotGenerator<Generator, Table>> tableIdGenerators = new ArrayList<>();

    public TableSnapshotGenerator() {
        super(Table.class, new Class[]{Schema.class});
        tableIdGenerators.add(new TableGeneratorSnapshotGenerator());
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (example.getSnapshotId() != null) {
            return example;
        }
        org.hibernate.mapping.Table hibernateTable = findHibernateTable(example, snapshot);
        if (hibernateTable == null) {
            return example;
        }

        Table table = new Table().setName(hibernateTable.getName());
        // !!!全局控制found信息输出
        if (GlobalSetting.isShowFoundInfo()) {
            Scope.getCurrentScope().getLog(getClass()).info("Found table " + table.getName());
        }
        table.setSchema(example.getSchema());
        if (hibernateTable.getComment() != null && !hibernateTable.getComment().isEmpty()) {
            table.setRemarks(hibernateTable.getComment());
        }
        // !!!原本要给表加注释的话，需要用@org.hibernate.annotations.Table(appliesTo = "表名",comment="注释")
        // 的方式，太麻烦了，所以增加了一个注解，直接在这里添加注释
        Class tableClass = getTableClass(snapshot, hibernateTable.getName());
        table.setAttribute(DatabaseObjectAttrName.TableClass, tableClass);
        if (tableClass != null && tableClass.isAnnotationPresent(TableComment.class)) {
            TableComment tableComment = (TableComment) tableClass.getAnnotation(TableComment.class);
            String oriComment = table.getRemarks();
            // 如果原来在metadata中有注释，就在原来的后面追加@TableComment中定义的注释
            if (oriComment == null) {
                table.setRemarks(tableComment.value());
            } else {
                table.setRemarks(oriComment + " " + tableComment.value());
            }
            // !!!增加附着在表注释的Meta数据
            TableRemarkMetaInfo metaInfo = new TableRemarkMetaInfo();
            metaInfo.setBuiltIn(true);
            table.setRemarks(TableRemarkMetaInfoUtil.setMetaInfo(table.getRemarks(), metaInfo));
        }
        if (tableClass != null && tableClass.isAnnotationPresent(Title.class)) {
            Title title = (Title) tableClass.getAnnotation(Title.class);
            table.setAttribute(DatabaseObjectAttrName.Title, title.value());
        }
        setLiquibaseTable(snapshot, table.getName(), table);

        return table;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Table.class)) {
            return;
        }

        if (foundObject instanceof Schema) {

            Schema schema = (Schema) foundObject;
            HibernateDatabase database = (HibernateDatabase) snapshot.getDatabase();
            MetadataImplementor metadata = (MetadataImplementor) database.getMetadata();

            Collection<PersistentClass> entityBindings = metadata.getEntityBindings();
            Iterator<PersistentClass> tableMappings = entityBindings.iterator();

            while (tableMappings.hasNext()) {
                PersistentClass pc = tableMappings.next();

                org.hibernate.mapping.Table hibernateTable = pc.getTable();
                if (hibernateTable.isPhysicalTable()) {
                    addDatabaseObjectToSchema(hibernateTable, schema, snapshot);

                    Collection<Join> joins = pc.getJoins();
                    Iterator<Join> joinMappings = joins.iterator();
                    while (joinMappings.hasNext()) {
                        Join join = joinMappings.next();
                        addDatabaseObjectToSchema(join.getTable(), schema, snapshot);
                    }
                }
            }

            Iterator<PersistentClass> classMappings = entityBindings.iterator();
            while (classMappings.hasNext()) {
                PersistentClass persistentClass = classMappings.next();
                if (!persistentClass.isInherited() && persistentClass.getIdentifier() instanceof SimpleValue) {
                    var simpleValue =  (SimpleValue) persistentClass.getIdentifier();
                    Generator ig = simpleValue.createGenerator(
                            metadata.getMetadataBuildingOptions().getIdentifierGeneratorFactory(),
                            database.getDialect(),
                            (RootClass) persistentClass
                    );
                    for (ExtendedSnapshotGenerator<Generator, Table> tableIdGenerator : tableIdGenerators) {
                        if (tableIdGenerator.supports(ig)) {
                            Table idTable = tableIdGenerator.snapshot(ig);
                            idTable.setSchema(schema);
                            schema.addDatabaseObject(snapshotObject(idTable, snapshot));
                            break;
                        }
                    }
                }
            }

            Collection<org.hibernate.mapping.Collection> collectionBindings = metadata.getCollectionBindings();
            Iterator<org.hibernate.mapping.Collection> collIter = collectionBindings.iterator();
            while (collIter.hasNext()) {
                org.hibernate.mapping.Collection coll = collIter.next();
                org.hibernate.mapping.Table hTable = coll.getCollectionTable();
                if (hTable.isPhysicalTable()) {
                    addDatabaseObjectToSchema(hTable, schema, snapshot);
                }
            }
        }
    }

    private void addDatabaseObjectToSchema(org.hibernate.mapping.Table join, Schema schema, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Table joinTable = new Table().setName(join.getName());
        joinTable.setSchema(schema);
        // !!!全局控制found信息输出
        if (GlobalSetting.isShowFoundInfo()) {
            Scope.getCurrentScope().getLog(getClass()).info("Found table " + joinTable.getName());
        }
        schema.addDatabaseObject(snapshotObject(joinTable, snapshot));
    }
}
