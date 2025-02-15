package org.laputa.rivulet.liquibase.database;

import liquibase.exception.DatabaseException;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.Dialect;

/**
 * Generic hibernate dialect used when an actual dialect cannot be determined.
 */
public class RivuletGenericDialect extends Dialect {
    public RivuletGenericDialect() throws DatabaseException {
        super(DatabaseVersion.make( 6, 1 ));
    }
}