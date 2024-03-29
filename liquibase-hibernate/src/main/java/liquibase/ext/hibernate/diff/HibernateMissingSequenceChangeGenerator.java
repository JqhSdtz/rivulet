package liquibase.ext.hibernate.diff;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.core.MissingSequenceChangeGenerator;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;

public class HibernateMissingSequenceChangeGenerator extends MissingSequenceChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Sequence.class.isAssignableFrom(objectType)) {
            return PRIORITY_ADDITIONAL;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        if (referenceDatabase instanceof HibernateDatabase && !comparisonDatabase.supportsSequences()) {
            return null;
        } else if (comparisonDatabase instanceof HibernateDatabase && !referenceDatabase.supportsSequences()) {
            return null;
        }else {
            return super.fixMissing(missingObject, control, referenceDatabase, comparisonDatabase, chain);
        }
    }
}
