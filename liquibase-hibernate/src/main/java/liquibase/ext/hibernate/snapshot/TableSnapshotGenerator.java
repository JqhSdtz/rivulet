package liquibase.ext.hibernate.snapshot;

import liquibase.Scope;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.GlobalSetting;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.ext.hibernate.snapshot.extension.ExtendedSnapshotGenerator;
import liquibase.ext.hibernate.snapshot.extension.MultipleHiLoPerTableSnapshotGenerator;
import liquibase.ext.hibernate.snapshot.extension.TableGeneratorSnapshotGenerator;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TableSnapshotGenerator extends HibernateSnapshotGenerator {

    private List<ExtendedSnapshotGenerator<IdentifierGenerator, Table>> tableIdGenerators = new ArrayList<ExtendedSnapshotGenerator<IdentifierGenerator, Table>>();

    public TableSnapshotGenerator() {
        super(Table.class, new Class[]{Schema.class});
        tableIdGenerators.add(new MultipleHiLoPerTableSnapshotGenerator());
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
        if (GlobalSetting.isShowFoundInfo()) {
            Scope.getCurrentScope().getLog(getClass()).info("Found table " + table.getName());
        }
        table.setSchema(example.getSchema());
        if (hibernateTable.getComment() != null && !hibernateTable.getComment().isEmpty()) {
            table.setRemarks(hibernateTable.getComment());
        }
        // !!!原本要给表加注释的话，需要用@org.hibernate.annotations.Table(appliesTo = "表名",comment="注释")
        // 的方式，太麻烦了，所以增加了一个注解，直接在这里添加注释
        Class tableClass = getTableClass(hibernateTable.getName());
        if (tableClass != null && tableClass.isAnnotationPresent(TableComment.class)) {
            TableComment tableComment = (TableComment) tableClass.getAnnotation(TableComment.class);
            String oriComment = table.getRemarks();
            // 如果原来在metadata中有注释，就在原来的后面追加@TableComment中定义的注释
            if (oriComment == null) {
                table.setRemarks(tableComment.value());
            } else {
                table.setRemarks(oriComment + " " + tableComment.value());
            }
        }

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
                    Table table = new Table().setName(hibernateTable.getName());
                    table.setSchema(schema);
                    if (GlobalSetting.isShowFoundInfo()) {
                        Scope.getCurrentScope().getLog(getClass()).info("Found table " + table.getName());
                    }
                    schema.addDatabaseObject(snapshotObject(table, snapshot));
                }
            }

            Iterator<PersistentClass> classMappings = entityBindings.iterator();
            while (classMappings.hasNext()) {
                PersistentClass persistentClass = (PersistentClass) classMappings
                        .next();
                if (!persistentClass.isInherited()) {
                    IdentifierGenerator ig = persistentClass.getIdentifier().createIdentifierGenerator(
                            metadata.getIdentifierGeneratorFactory(),
                            database.getDialect(),
                            null,
                            null,
                            (RootClass) persistentClass
                    );
                    for (ExtendedSnapshotGenerator<IdentifierGenerator, Table> tableIdGenerator : tableIdGenerators) {
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
                    Table table = new Table().setName(hTable.getName());
                    table.setSchema(schema);
                    if (GlobalSetting.isShowFoundInfo()) {
                        Scope.getCurrentScope().getLog(getClass()).info("Found table " + table.getName());
                    }
                    schema.addDatabaseObject(snapshotObject(table, snapshot));
                }
            }
        }
    }
}
