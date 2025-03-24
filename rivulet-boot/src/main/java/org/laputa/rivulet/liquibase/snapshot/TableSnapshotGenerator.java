package org.laputa.rivulet.liquibase.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.laputa.rivulet.liquibase.database.RivuletDatabase;
import org.laputa.rivulet.module.dbms_model.entity.RvTable;

import java.util.List;

public class TableSnapshotGenerator extends RivuletSnapshotGenerator {

    public TableSnapshotGenerator() {
        super(Table.class, new Class[]{Schema.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (example.getSnapshotId() != null) {
            return example;
        }
        RvTable rvTable = findRvTable(example, snapshot);
        if (rvTable == null) {
            return example;
        }
        Table table = new Table().setName(rvTable.getCode());
        table.setSchema(example.getSchema());
        if (rvTable.getRemark() != null && !rvTable.getRemark().isEmpty()) {
            table.setRemarks(rvTable.getRemark());
        }
        return table;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Table.class)) {
            return;
        }
        if (foundObject instanceof Schema schema) {
            RivuletDatabase database = (RivuletDatabase) snapshot.getDatabase();
            List<RvTable> rvTables = database.getRvTables();
            for (RvTable rvTable : rvTables) {
                addDatabaseObjectToSchema(rvTable, schema, snapshot);
            }
        }
    }

    private void addDatabaseObjectToSchema(RvTable join, Schema schema, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Table joinTable = new Table().setName(join.getCode());
        joinTable.setSchema(schema);
        schema.addDatabaseObject(snapshotObject(joinTable, snapshot));
    }
}
