package org.laputa.rivulet.liquibase.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import org.laputa.rivulet.module.dbms_model.entity.RvIndex;
import org.laputa.rivulet.module.dbms_model.entity.RvTable;
import org.laputa.rivulet.module.dbms_model.entity.column_relation.RvIndexColumn;

public class IndexSnapshotGenerator extends RivuletSnapshotGenerator {

    @SuppressWarnings("unchecked")
    public IndexSnapshotGenerator() {
        super(Index.class, new Class[]{Table.class, ForeignKey.class, UniqueConstraint.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (example.getSnapshotId() != null) {
            return example;
        }
        Relation table = ((Index) example).getRelation();
        RvTable rvTable = findRvTable(table, snapshot);
        if (rvTable == null) {
            return example;
        }
        for (RvIndex rvIndex : rvTable.getIndexes()) {
            Index index = handleRvIndex(table, rvIndex);
            if (index.getColumnNames().equalsIgnoreCase(((Index) example).getColumnNames())) {
                table.getIndexes().add(index);
                return index;
            }
        }
        return example;

    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Index.class)) {
            return;
        }
        if (foundObject instanceof Table table) {
            RvTable rvTable = findRvTable(table, snapshot);
            if (rvTable == null) {
                return;
            }
            for (RvIndex rvIndex : rvTable.getIndexes()) {
                Index index = handleRvIndex(table, rvIndex);
                table.getIndexes().add(index);
            }
        }
    }

    private Index handleRvIndex(Relation table, RvIndex rvIndex) {
        Index index = new Index();
        index.setRelation(table);
        index.setName(rvIndex.getCode());
        index.setUnique(rvIndex.getUniqueIndex());
        for (RvIndexColumn rvIndexColumn : rvIndex.getIndexColumns()) {
            Boolean descending = rvIndexColumn.getColumn().getDescending();
            Column column = new Column(rvIndexColumn.getColumn().getCode());
            index.getColumns().add(column.setRelation(table).setDescending(descending));
        }
        return index;
    }

}
