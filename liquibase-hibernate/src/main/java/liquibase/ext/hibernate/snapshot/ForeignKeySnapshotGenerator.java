package liquibase.ext.hibernate.snapshot;

import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.DatabaseObjectAttrName;
import liquibase.ext.hibernate.util.IndexStoreUtil;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

public class ForeignKeySnapshotGenerator extends HibernateSnapshotGenerator {

    public ForeignKeySnapshotGenerator() {
        super(ForeignKey.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(ForeignKey.class)) {
            return;
        }
        if (foundObject instanceof Table) {
            Table currentTable = (Table) foundObject;
            org.hibernate.mapping.Table hibernateTable = findHibernateTable(currentTable, snapshot);

            for (var hibernateForeignKey : hibernateTable.getForeignKeys().values()) {
                currentTable.setSchema(hibernateTable.getCatalog(), hibernateTable.getSchema());
                org.hibernate.mapping.Table hibernateReferencedTable = hibernateForeignKey.getReferencedTable();
                Table referencedTable = getLiquibaseTable(snapshot, hibernateReferencedTable.getName());
                if (referencedTable == null) referencedTable = new Table().setName(hibernateReferencedTable.getName());
                referencedTable.setSchema(hibernateReferencedTable.getCatalog(), hibernateReferencedTable.getSchema());

                if (hibernateForeignKey.isCreationEnabled() && hibernateForeignKey.isPhysicalConstraint()) {
                    ForeignKey fk = new ForeignKey();
                    // !!!设置foreignKey的title
                    String currentTableTitle = currentTable.getAttribute(DatabaseObjectAttrName.Title, String.class);
                    String referencedTableTitle = referencedTable.getAttribute(DatabaseObjectAttrName.Title, String.class);
                    fk.setAttribute(DatabaseObjectAttrName.Title, "从" + currentTableTitle + "到" + referencedTableTitle + "的外键关联");

                    fk.setName(hibernateForeignKey.getName());
                    fk.setPrimaryKeyTable(referencedTable);
                    fk.setForeignKeyTable(currentTable);
                    for (Object column : hibernateForeignKey.getColumns()) {
                        fk.addForeignKeyColumn(new liquibase.structure.core.Column(((org.hibernate.mapping.Column) column).getName()));
                    }
                    for (Object column : hibernateForeignKey.getReferencedColumns()) {
                        fk.addPrimaryKeyColumn(new liquibase.structure.core.Column(((org.hibernate.mapping.Column) column).getName()));
                    }
                    if (fk.getPrimaryKeyColumns() == null || fk.getPrimaryKeyColumns().isEmpty()) {
                        for (Object column : hibernateReferencedTable.getPrimaryKey().getColumns()) {
                            fk.addPrimaryKeyColumn(new liquibase.structure.core.Column(((org.hibernate.mapping.Column) column).getName()));
                        }
                    }

                    fk.setDeferrable(false);
                    fk.setInitiallyDeferred(false);

                    // !!!这段原来被注释掉了，但是我觉得为外键创建索引还是有必要的
                    Index index;
                    // 如果此前已经有对应的index，则直接获取原本的index。该index可能是由foreignKey或primaryKey创建而来
                    Index oriIndex = IndexStoreUtil.getIndex(fk.getForeignKeyColumns());
                    if (oriIndex != null) {
                        index = oriIndex;
                    } else {
                        index = new Index();
                        index.setName("IX_" + fk.getName());
                        index.setAttribute(DatabaseObjectAttrName.Title, fk.getAttribute(DatabaseObjectAttrName.Title, String.class) + "的索引");
                        index.setRelation(currentTable);
                        index.setColumns(fk.getForeignKeyColumns());
                        // unique默认为false，如果有唯一性约束的话，在UniqueConstrainSnapshotGenerator里会拿出来这个索引并修改unique值
                        index.setUnique(false);
                        IndexStoreUtil.addIndex(fk.getForeignKeyColumns(), index);
                        currentTable.getIndexes().add(index);
                    }
                    fk.setBackingIndex(index);

                    currentTable.getOutgoingForeignKeys().add(fk);
                    currentTable.getSchema().addDatabaseObject(fk);
                }
            }

        }
    }

}
