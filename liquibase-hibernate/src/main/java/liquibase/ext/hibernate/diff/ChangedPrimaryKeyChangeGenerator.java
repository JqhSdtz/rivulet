package liquibase.ext.hibernate.diff;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.PrimaryKey;

/**
 * Hibernate doesn't know about all the variations that occur with primary keys, especially backing index stuff.
 * To prevent changing customized primary keys, we suppress this kind of changes from hibernate side.
 */
public class ChangedPrimaryKeyChangeGenerator extends liquibase.diff.output.changelog.core.ChangedPrimaryKeyChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_ADDITIONAL;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        DifferenceFilter.filter(differences);
        if (referenceDatabase instanceof HibernateDatabase || comparisonDatabase instanceof HibernateDatabase) {
            differences.removeDifference("unique");
            differences.removeDifference("validate");
            if (!differences.hasDifferences()) {
                return null;
            }
        }

        return super.fixChanged(changedObject, differences, control, referenceDatabase, comparisonDatabase, chain);
    }
}
