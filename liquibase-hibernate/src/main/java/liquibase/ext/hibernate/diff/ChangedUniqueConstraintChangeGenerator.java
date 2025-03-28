package liquibase.ext.hibernate.diff;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.UniqueConstraint;

/**
 * Unique attribute for unique constraints backing index can have different values dependending on the database implementation,
 * so we suppress all unique constraint changes based on unique constraints.

 */
public class ChangedUniqueConstraintChangeGenerator extends liquibase.diff.output.changelog.core.ChangedUniqueConstraintChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
            return PRIORITY_ADDITIONAL;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        DifferenceFilter.filter(differences);
        if (referenceDatabase instanceof HibernateDatabase || comparisonDatabase instanceof HibernateDatabase) {
            differences.removeDifference("unique");
            if (!differences.hasDifferences()) {
                return null;
            }
        }
        return super.fixChanged(changedObject, differences, control, referenceDatabase, comparisonDatabase, chain);
    }
}
