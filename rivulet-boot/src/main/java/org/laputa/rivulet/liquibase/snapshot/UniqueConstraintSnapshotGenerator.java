package org.laputa.rivulet.liquibase.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import org.laputa.rivulet.module.dbms_model.entity.RvTable;
import org.laputa.rivulet.module.dbms_model.entity.column_relation.RvUniqueColumn;
import org.laputa.rivulet.module.dbms_model.entity.constraint.RvUnique;

public class UniqueConstraintSnapshotGenerator extends RivuletSnapshotGenerator {

    public UniqueConstraintSnapshotGenerator() {
        super(UniqueConstraint.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!(snapshot.getSnapshotControl().shouldInclude(UniqueConstraint.class)
                && foundObject instanceof Table)) {
            return;
        }
        Table table = (Table) foundObject;
        RvTable prototype = findRvPrototype(table, snapshot);
        if (prototype == null) {
            return;
        }
        for (RvUnique rvUnique : prototype.getUniques()) {
            UniqueConstraint uniqueConstraint = new UniqueConstraint();
            uniqueConstraint.setName(rvUnique.getCode());
            uniqueConstraint.setRelation(table);
            uniqueConstraint.setClustered(false); // No way to set true via Hibernate
            int i = 0;
            for (RvUniqueColumn rvUniqueColumn : rvUnique.getUniqueColumns()) {
                uniqueConstraint.addColumn(i++, new Column(rvUniqueColumn.getColumn().getCode()).setRelation(table));
            }
            Index index = getBackingIndex(uniqueConstraint, rvUnique);
            uniqueConstraint.setBackingIndex(index);
            table.getUniqueConstraints().add(uniqueConstraint);
        }
    }

    protected Index getBackingIndex(UniqueConstraint uniqueConstraint, RvUnique rvUnique) {
        Index index = new Index();
        index.setRelation(uniqueConstraint.getRelation());
        index.setColumns(uniqueConstraint.getColumns());
        index.setUnique(true);
        index.setName(rvUnique.getBackingIndex().getCode());
        return index;
    }

}
