package org.laputa.rivulet.liquibase.snapshot;

import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.snapshot.HibernateSnapshotGenerator;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;
import org.laputa.rivulet.liquibase.database.RivuletDatabase;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvForeignKeyForeignColumn;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvForeignKeyTargetColumn;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvPrimaryKeyColumn;
import org.laputa.rivulet.module.data_model.entity.constraint.RvForeignKey;

public class ForeignKeySnapshotGenerator extends RivuletSnapshotGenerator {

    public ForeignKeySnapshotGenerator() {
        super(ForeignKey.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!(snapshot.getSnapshotControl().shouldInclude(ForeignKey.class)
                && foundObject instanceof Table)) {
            return;
        }
        Table table = (Table) foundObject;
        RivuletDatabase database = (RivuletDatabase) snapshot.getDatabase();
        for (RvPrototype prototype : database.getPrototypes()) {
            for (RvForeignKey rvForeignKey : prototype.getForeignKeys()) {
                Table currentTable = new Table().setName(prototype.getName());
                currentTable.setSchema(database.getDefaultCatalogName(), database.getDefaultSchemaName());
                RvPrototype targetPrototype = rvForeignKey.getTargetPrototype();
                Table targetTable = new Table().setName(targetPrototype.getName());
                targetTable.setSchema(database.getDefaultCatalogName(), database.getDefaultSchemaName());
                ForeignKey fk = new ForeignKey();
                fk.setName(rvForeignKey.getName());
                fk.setPrimaryKeyTable(targetTable);
                fk.setForeignKeyTable(currentTable);
                for (RvForeignKeyForeignColumn keyForeignColumn : rvForeignKey.getForeignKeyForeignColumns()) {
                    fk.addForeignKeyColumn(new Column((keyForeignColumn.getColumn().getName())));
                }
                for (RvForeignKeyTargetColumn keyTargetColumn : rvForeignKey.getForeignKeyTargetColumns()) {
                    fk.addPrimaryKeyColumn(new Column((keyTargetColumn).getColumn().getName()));
                }
                if (fk.getPrimaryKeyColumns() == null || fk.getPrimaryKeyColumns().isEmpty()) {
                    for (RvPrimaryKeyColumn primaryKeyColumn : targetPrototype.getPrimaryKey().getPrimaryKeyColumns()) {
                        fk.addPrimaryKeyColumn(new Column(primaryKeyColumn.getColumn().getName()));
                    }
                }
                fk.setDeferrable(false);
                fk.setInitiallyDeferred(false);
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(currentTable, table, null, database)) {
                    table.getOutgoingForeignKeys().add(fk);
                    table.getSchema().addDatabaseObject(fk);
                }
            }
        }
    }

}
