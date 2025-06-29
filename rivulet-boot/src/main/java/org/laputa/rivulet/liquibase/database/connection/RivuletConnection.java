package org.laputa.rivulet.liquibase.database.connection;

import liquibase.resource.ResourceAccessor;
import lombok.Getter;
import org.laputa.rivulet.common.constant.Strings;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Implements java.sql.Connection in order to pretend a rivulet configuration is a database in order to fit into the Liquibase framework.
 * Beyond standard Connection methods, this class exposes {@link #getPrefix()}, {@link #getPath()} and {@link #getProperties()} to access the setting passed in the JDBC URL.
 */
@Getter
public class RivuletConnection implements Connection {
    /**
     * -- GETTER --
     *  Returns the 'protocol' of the URL. For example, "hibernate:classic" or "hibernate:ejb3"
     */
    private final String prefix;
    /**
     * -- GETTER --
     *  Returns the entire connection URL
     */
    private final String url;

    /**
     * -- GETTER --
     *  The portion of the url between the path and the query string. Normally a filename or a class name.
     */
    private String path;
    private final ResourceAccessor resourceAccessor;
    /**
     * -- GETTER --
     *  The set of properties provided by the URL. Eg:
     *  <p/>
     *  <code>hibernate:classic:/path/to/hibernate.cfg.xml?foo=bar</code>
     *  <p/>
     *  This will have a property called 'foo' with a value of 'bar'.
     */
    private final Properties properties;

    public RivuletConnection(String url, ResourceAccessor resourceAccessor) {
        this.url = url;

        this.prefix = url.replaceFirst(":[^:]+$", "");

        // Trim the prefix off the URL for the path
        path = url.substring(prefix.length() + 1);
        this.resourceAccessor = resourceAccessor;

        // Check if there is a parameter/query string value.
        properties = new Properties();

        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            // Convert the query string into properties
            properties.putAll(readProperties(path.substring(queryIndex + 1)));

            if (properties.containsKey("dialect") && !properties.containsKey("rivulet.dialect")) {
                properties.put("rivulet.dialect", properties.getProperty("dialect"));
            }

            // Remove the query string
            path = path.substring(0, queryIndex);
        }
    }

    /**
     * Creates properties to attach to this connection based on the passed query string.
     */
    protected Properties readProperties(String queryString) {
        Properties properties = new Properties();
        queryString = queryString.replaceAll("&", System.lineSeparator());
        try {
            queryString = URLDecoder.decode(queryString, StandardCharsets.UTF_8);
            properties.load(new StringReader(queryString));
        } catch (IOException ioe) {
            throw new IllegalStateException("Failed to read properties from url", ioe);
        }

        return properties;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// JDBC METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Statement createStatement() throws SQLException {
        return null;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return null;
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return null;
    }

    public String nativeSQL(String sql) throws SQLException {
        return null;
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {

    }

    public boolean getAutoCommit() throws SQLException {
        return false;
    }

    public void commit() throws SQLException {

    }

    public void rollback() throws SQLException {

    }

    public void close() throws SQLException {

    }

    public boolean isClosed() throws SQLException {
        return false;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return new RivuletConnectionMetadata(url);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {

    }

    public boolean isReadOnly() throws SQLException {
        return true;
    }

    public void setCatalog(String catalog) throws SQLException {

    }

    public String getCatalog() throws SQLException {
        return Strings.RIVULET;
    }

    public void setTransactionIsolation(int level) throws SQLException {

    }

    public int getTransactionIsolation() throws SQLException {
        return 0;
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void clearWarnings() throws SQLException {

    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

    }

    public void setHoldability(int holdability) throws SQLException {

    }

    public int getHoldability() throws SQLException {
        return 0;
    }

    public Savepoint setSavepoint() throws SQLException {
        return null;
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return null;
    }

    public void rollback(Savepoint savepoint) throws SQLException {

    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {

    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return null;
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return null;
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return null;
    }

    public Clob createClob() throws SQLException {
        return null;
    }

    public Blob createBlob() throws SQLException {
        return null;
    }

    public NClob createNClob() throws SQLException {
        return null;
    }

    public SQLXML createSQLXML() throws SQLException {
        return null;
    }

    public boolean isValid(int timeout) throws SQLException {
        return false;
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {

    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {

    }

    public String getClientInfo(String name) throws SQLException {
        return null;
    }

    public Properties getClientInfo() throws SQLException {
        return null;
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return null;
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return null;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    //@Override only in java 1.7
    public void abort(Executor arg0) throws SQLException {
    }

    //@Override only in java 1.7
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    //@Override only in java 1.7
    public String getSchema() throws SQLException {
        return Strings.RIVULET;
    }

    //@Override only in java 1.7
    public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
    }

    //@Override only in java 1.7
    public void setSchema(String arg0) throws SQLException {
    }

}
