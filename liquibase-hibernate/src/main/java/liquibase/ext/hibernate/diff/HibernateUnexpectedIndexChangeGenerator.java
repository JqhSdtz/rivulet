package liquibase.ext.hibernate.diff;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.core.UnexpectedIndexChangeGenerator;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;

/**
 * Indexes tend to be added in the database that don't correspond to what is in Hibernate, so we suppress all dropIndex changes
 * based on indexes defined in the database but not in hibernate.
 */
public class HibernateUnexpectedIndexChangeGenerator extends UnexpectedIndexChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_ADDITIONAL;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        if (referenceDatabase instanceof HibernateDatabase || comparisonDatabase instanceof HibernateDatabase) {
            return null;
        } else {
            return super.fixUnexpected(unexpectedObject, control, referenceDatabase, comparisonDatabase, chain);
        }
    }
}
