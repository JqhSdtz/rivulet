package org.laputa.rivulet.liquibase.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import org.hibernate.sql.Alias;
import org.laputa.rivulet.module.dbms_model.entity.RvIndex;
import org.laputa.rivulet.module.dbms_model.entity.RvTable;
import org.laputa.rivulet.module.dbms_model.entity.column_relation.RvPrimaryKeyColumn;
import org.laputa.rivulet.module.dbms_model.entity.constraint.RvPrimaryKey;

public class PrimaryKeySnapshotGenerator extends RivuletSnapshotGenerator {

    private static final int PK_NAME_LENGTH = 63;
    private static final String PK_NAME_SUFFIX = "PK";
    private static final Alias PK_NAME_ALIAS = new Alias(PK_NAME_LENGTH, PK_NAME_SUFFIX);

    public PrimaryKeySnapshotGenerator() {
        super(PrimaryKey.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!(snapshot.getSnapshotControl().shouldInclude(PrimaryKey.class)
                && foundObject instanceof Table)) {
            return;
        }
        Table table = (Table) foundObject;
        RvTable prototype = findRvPrototype(table, snapshot);
        if (prototype == null) {
            return;
        }
        RvPrimaryKey rvPrimaryKey = prototype.getPrimaryKey();
        if (rvPrimaryKey == null) {
            return;
        }
        PrimaryKey pk = new PrimaryKey();
        pk.setName(rvPrimaryKey.getCode());
        pk.setTable(table);
        for (RvPrimaryKeyColumn rvPrimaryKeyColumn : rvPrimaryKey.getPrimaryKeyColumns()) {
            pk.getColumns().add(new Column(rvPrimaryKeyColumn.getColumn().getCode()).setRelation(table));
        }
        table.setPrimaryKey(pk);
        Index index = new Index();
        RvIndex backingIndex = rvPrimaryKey.getBackingIndex();
        index.setName(backingIndex.getCode());
        index.setRelation(table);
        index.setColumns(pk.getColumns());
        index.setUnique(true);
        pk.setBackingIndex(index);
//        table.getIndexes().add(index);
    }
}
