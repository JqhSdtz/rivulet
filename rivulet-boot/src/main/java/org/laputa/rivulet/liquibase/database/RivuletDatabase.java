package org.laputa.rivulet.liquibase.database;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ReflectUtil;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.DatabaseConnectionInfo;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.type.spi.TypeConfiguration;
import org.laputa.rivulet.common.util.SpringBeanUtil;
import org.laputa.rivulet.common.util.TypeConvertUtil;
import org.laputa.rivulet.liquibase.database.connection.RvDriver;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.repository.RvPrototypeRepository;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class RivuletDatabase extends AbstractJdbcDatabase {

    @Getter
    private List<RvPrototype> prototypes;
    private TypeConfiguration typeConfiguration;
    private Dialect dbmsDialect;
    private Method resolveSqlTypeCodeMethod;

    private boolean indexesForForeignKeys = false;
    public static final String DEFAULT_SCHEMA = "RIVULET";

    public RivuletDatabase() {
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


    @SneakyThrows
    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);
        Scope.getCurrentScope().getLog(getClass()).info("Reading rivulet configuration " + getConnection().getURL());
        RvPrototypeRepository rvPrototypeRepository = SpringBeanUtil.getBean(RvPrototypeRepository.class);
        JpaProperties jpaProperties = SpringBeanUtil.getBean(JpaProperties.class);
        this.typeConfiguration = new TypeConfiguration();
        String dialectClassName = jpaProperties.getProperties().get("hibernate.dialect");
        if (dialectClassName != null) {
            Class<? extends Dialect> dialectClass = TypeConvertUtil.convert(new TypeReference<>() {
            }, Class.forName(dialectClassName));
            dbmsDialect = dialectClass.getDeclaredConstructor().newInstance();
            this.resolveSqlTypeCodeMethod = ReflectUtil.getMethodByName(dialectClass, "resolveSqlTypeCode");
        }
        prototypes = rvPrototypeRepository.findAll();
        afterSetup();
    }

    @SneakyThrows
    public Integer resolveSqlTypeCode(String columnTypeName) {
        if (resolveSqlTypeCodeMethod == null) return null;
        return (Integer) resolveSqlTypeCodeMethod.invoke(columnTypeName, typeConfiguration);
    }

    /**
     * Perform any post-configuration setting logic.
     */
    protected void afterSetup() {
        if (dbmsDialect instanceof MySQLDialect) {
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
        public boolean isUnwrappableAs(@NonNull Class unwrapType) {
            return false;
        }

        @Override
        public <T> T unwrap(@NonNull Class<T> unwrapType) {
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
