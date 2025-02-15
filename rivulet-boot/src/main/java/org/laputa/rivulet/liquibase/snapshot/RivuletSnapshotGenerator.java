package org.laputa.rivulet.liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.structure.DatabaseObject;
import org.laputa.rivulet.liquibase.database.RivuletDatabase;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;

import java.util.List;

/**
 * Base class for all Rivulet SnapshotGenerators
 */
public abstract class RivuletSnapshotGenerator implements SnapshotGenerator {

    private static final int PRIORITY_RIVULET_ADDITIONAL = 199;
    private static final int PRIORITY_RIVULET_DEFAULT = 99;

    private final Class<? extends DatabaseObject> defaultFor;
    private Class<? extends DatabaseObject>[] addsTo = null;

    protected RivuletSnapshotGenerator(Class<? extends DatabaseObject> defaultFor) {
        this.defaultFor = defaultFor;
    }

    protected RivuletSnapshotGenerator(Class<? extends DatabaseObject> defaultFor, Class<? extends DatabaseObject>[] addsTo) {
        this.defaultFor = defaultFor;
        this.addsTo = addsTo;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return null;
    }

    @Override
    public final int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof RivuletDatabase) {
            if (defaultFor != null && defaultFor.isAssignableFrom(objectType)) {
                return PRIORITY_RIVULET_DEFAULT;
            }
            if (addsTo() != null) {
                for (Class<? extends DatabaseObject> type : addsTo()) {
                    if (type.isAssignableFrom(objectType)) {
                        return PRIORITY_RIVULET_ADDITIONAL;
                    }
                }
            }
        }
        // PRIORITY_NONE表示不执行，用于过滤不属于RivuletDatabase的snapshot请求
        return PRIORITY_NONE;
    }

    @Override
    public final Class<? extends DatabaseObject>[] addsTo() {
        return addsTo;
    }

    @Override
    public final DatabaseObject snapshot(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        if (defaultFor != null && defaultFor.isAssignableFrom(example.getClass())) {
            return snapshotObject(example, snapshot);
        }
        DatabaseObject chainResponse = chain.snapshot(example, snapshot);
        if (chainResponse == null) {
            return null;
        }
        if (addsTo() != null) {
            for (Class<? extends DatabaseObject> addType : addsTo()) {
                if (addType.isAssignableFrom(example.getClass())) {
                    addTo(chainResponse, snapshot);
                }
            }
        }
        return chainResponse;
    }

    protected abstract DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException;

    protected abstract void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException;

    protected RvPrototype findRvPrototype(DatabaseObject example, DatabaseSnapshot snapshot) {
        RivuletDatabase database = (RivuletDatabase) snapshot.getDatabase();
        List<RvPrototype> prototypes = database.getPrototypes();
        for (RvPrototype prototype : prototypes) {
            if (prototype.getName().equalsIgnoreCase(example.getName())) {
                return prototype;
            }
        }
        return null;
    }
}
