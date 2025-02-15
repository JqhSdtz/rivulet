package org.laputa.rivulet.liquibase.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.laputa.rivulet.liquibase.database.RivuletDatabase;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;

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
        RvPrototype prototype = findRvPrototype(example, snapshot);
        if (prototype == null) {
            return example;
        }
        Table table = new Table().setName(prototype.getName());
        table.setSchema(example.getSchema());
        if (prototype.getRemark() != null && !prototype.getRemark().isEmpty()) {
            table.setRemarks(prototype.getRemark());
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
            List<RvPrototype> prototypes = database.getPrototypes();
            for (RvPrototype prototype : prototypes) {
                addDatabaseObjectToSchema(prototype, schema, snapshot);
            }
        }
    }

    private void addDatabaseObjectToSchema(RvPrototype join, Schema schema, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Table joinTable = new Table().setName(join.getName());
        joinTable.setSchema(schema);
        schema.addDatabaseObject(snapshotObject(joinTable, snapshot));
    }
}
