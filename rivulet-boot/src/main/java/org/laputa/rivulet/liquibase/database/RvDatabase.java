package org.laputa.rivulet.liquibase.database;

import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.DatabaseConnectionInfo;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.laputa.rivulet.common.util.SpringBeanUtil;
import org.laputa.rivulet.liquibase.database.connection.RvDriver;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.repository.RvPrototypeRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class RvDatabase extends AbstractJdbcDatabase {

    private Metadata metadata;

    private List<RvPrototype> prototypes;
    protected Dialect dialect;

    private boolean indexesForForeignKeys = false;
    public static final String DEFAULT_SCHEMA = "RIVULET";

    public RvDatabase() {
        setDefaultCatalogName(DEFAULT_SCHEMA);
        setDefaultSchemaName(DEFAULT_SCHEMA);
    }

    public boolean requiresPassword() {
        return false;
    }

    public boolean requiresUsername() {
        return false;
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("rivulet")) {
            return RvDriver.class.getName();
        }
        return null;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }


    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);
        Scope.getCurrentScope().getLog(getClass()).info("Reading rivulet configuration " + getConnection().getURL());
        RvPrototypeRepository rvPrototypeRepository = SpringBeanUtil.getBean(RvPrototypeRepository.class);
        prototypes = rvPrototypeRepository.findAll();
        afterSetup();
    }

    public List<RvPrototype> getPrototypes() {
        return prototypes;
    }

    /**
     * Returns the dialect determined during database initialization.
     */
    public Dialect getDialect() {
        return dialect;
    }

    public Metadata getMetadata() throws DatabaseException {
        return metadata;
    }

    /**
     * Perform any post-configuration setting logic.
     */
    protected void afterSetup() {
        if (dialect instanceof MySQLDialect) {
            indexesForForeignKeys = true;
        }
    }


    @Override
    public boolean createsIndexesForForeignKeys() {
        return indexesForForeignKeys;
    }

    @Override
    public Integer getDefaultPort() {
        return 0;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    protected String getConnectionCatalogName() throws DatabaseException {
        return getDefaultCatalogName();
    }

    @Override
    protected String getConnectionSchemaName() {
        return getDefaultSchemaName();
    }

    @Override
    public String getDefaultSchemaName() {
        return DEFAULT_SCHEMA;
    }

    @Override
    public String getDefaultCatalogName() {
        return DEFAULT_SCHEMA;
    }

    @Override
    public boolean isSafeToRunUpdate() throws DatabaseException {
        return true;
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }

    @Override
    public boolean supportsSchemas() {
        return true;
    }

    @Override
    public boolean supportsCatalogs() {
        return false;
    }

    /**
     * Used by hibernate to ensure no database access is performed.
     */
    static class NoOpConnectionProvider implements ConnectionProvider, MultiTenantConnectionProvider {

        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("No connection");
        }

        @Override
        public void closeConnection(Connection conn) throws SQLException {

        }

        @Override
        public boolean supportsAggressiveRelease() {
            return false;
        }

        @Override
        public DatabaseConnectionInfo getDatabaseConnectionInfo(Dialect dialect) {
            return ConnectionProvider.super.getDatabaseConnectionInfo(dialect);
        }

        @Override
        public boolean isUnwrappableAs(Class unwrapType) {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> unwrapType) {
            return null;
        }

        @Override
        public Connection getAnyConnection() throws SQLException {
            return getConnection();
        }

        @Override
        public void releaseAnyConnection(Connection connection) throws SQLException {

        }

        @Override
        public Connection getConnection(Object tenantIdentifier) throws SQLException {
            return null;
        }

        @Override
        public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {

        }

    }
}
