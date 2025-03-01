package liquibase.ext.hibernate.diff;

import liquibase.diff.ObjectDifferences;
import liquibase.ext.hibernate.DatabaseObjectAttrName;

public class DifferenceFilter {
    public static void filter(ObjectDifferences differences) {
        differences.removeDifference(DatabaseObjectAttrName.Title);
        differences.removeDifference(DatabaseObjectAttrName.IndexStoreMap);
    }
}
