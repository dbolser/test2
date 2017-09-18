/*
 * Copyright [2009-2014] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.genomeloader.services.sql.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.ensembl.genomeloader.services.ServiceException;
import org.ensembl.genomeloader.services.config.ServiceConfig;
import org.ensembl.genomeloader.services.sql.DatabaseConnection;
import org.ensembl.genomeloader.services.sql.SqlServiceException;
import org.ensembl.genomeloader.util.ActiveAwareGenericKeyedObjectPool;
import org.ensembl.genomeloader.util.DatabaseSettings;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.sql.DbUtils;

/**
 * service class providing access for SQL queries onto RDBMS
 *
 * @author dstaines
 *
 */
public class LocalSqlService extends AbstractConnectionBackedSqlService {

    protected class ConnectionFactory implements KeyedPoolableObjectFactory {

        private Map<DatabaseSettings, Boolean> driverInitalisationMap = new EnumMap<DatabaseSettings, Boolean>(
                DatabaseSettings.class);
        private final LocalSqlService srv;

        public ConnectionFactory(LocalSqlService srv) {
            this.srv = srv;
            for (DatabaseSettings setting : DatabaseSettings.values()) {
                driverInitalisationMap.put(setting, Boolean.FALSE);
            }
        }

        public void activateObject(Object uri, Object conn) throws Exception {
            getLog().trace("Activating connection to " + uri);
            ((DatabaseConnection) conn).setActive(true);
        }

        public void destroyObject(Object uri, Object conn) throws Exception {
            getLog().trace("Destroying connection to " + uri);
            ((Connection) conn).close();
        }

        public Object makeObject(Object uri) throws Exception {
            DatabaseSettings db = DatabaseSettings.getSettingsForUri((String) uri);
            if (!driverInitalisationMap.get(db)) {
                Class.forName(db.getDriver());
                driverInitalisationMap.put(db, Boolean.TRUE);
            }
            getLog().trace("Opening connection to " + uri);
            return new DatabaseConnection(DriverManager.getConnection((String) uri), (String) uri, srv,
                    statementCacheSize);
        }

        public void passivateObject(Object uri, Object conn) throws Exception {
            getLog().trace("Releasing connection to " + uri);
            DatabaseConnection con = (DatabaseConnection) conn;
            con.clearResults();
            con.setReadyToClose(false);
            con.setPendingResults(false);
            DbUtils.rollback(con);
            DbUtils.turnOnAutoCommit(con);
            con.setActive(false);
        }

        public boolean validateObject(Object uri, Object conn) {
            try {
                getLog().trace("Validating connection to " + uri);
                // a valid connection:
                // 1. is not null
                // 2. has not been invalidated
                // 3. has not been closed
                return conn != null && ((DatabaseConnection) conn).isValid() && !((DatabaseConnection) conn).isClosed();
            } catch (SQLException e) {
                return false;
            }
        }
    }

    public static final int DEFAULT_MAX_DB_CON = 5;

    public static final int DEFAULT_MAX_DB_CON_TOTAL = 30;

    public static final int DEFAULT_DB_IDLETIME = 30000;

    public static final int DEFAULT_EVICTION_RUN_TIME = 5000;

    /**
     * connection pool keyed by URI
     */
    private ActiveAwareGenericKeyedObjectPool connectionPool = null;
    private Log log = null;

    /* pool construction parameters */
    protected int statementCacheSize = 20;

    /**
     * Simple constructor which uses default parameters for pool. No
     * ServiceContext is required
     */
    public LocalSqlService() {
        this(DEFAULT_MAX_DB_CON, DEFAULT_MAX_DB_CON_TOTAL, DEFAULT_DB_IDLETIME, DEFAULT_EVICTION_RUN_TIME,
                DEFAULT_MAX_STATEMENT_CACHE);
    }

    /**
     * Directly parameterised constructor for which no ServiceContext is
     * required.
     *
     * @param maxDbConnections
     *            maximum number of connections per unique URI
     * @param maxDbConnectionsTotal
     *            maximum number of connections for all URIs
     * @param dbIdleTime
     *            time in milliseconds for which an idle connection remains open
     *            and available
     * @param statementCacheSize
     *            number of prepared statements to cache for each connection
     */
    public LocalSqlService(int maxDbConnections, int maxDbConnectionsTotal, int dbIdleTime, int evictionRunTime,
            int statementCacheSize) {
        super(statementCacheSize);
        // open a new pool - should parameterise from context
        ActiveAwareGenericKeyedObjectPool.Config config = new GenericKeyedObjectPool.Config();
        config.maxActive = maxDbConnections;
        config.maxTotal = maxDbConnectionsTotal;
        config.minEvictableIdleTimeMillis = dbIdleTime;
        config.timeBetweenEvictionRunsMillis = evictionRunTime;
        config.testWhileIdle = false;
        config.testOnReturn = true;
        config.testOnBorrow = true;
        connectionPool = new ActiveAwareGenericKeyedObjectPool(new ConnectionFactory(this), config);
    }

    /**
     * Construct pool using parameters from supplied config
     *
     * @param config
     *            config to use
     */
    public LocalSqlService(ServiceConfig config) {
        this(config.getMaxDbConnections(), config.getMaxDbConnectionsTotal(), config.getDbIdleTime(),
                DEFAULT_EVICTION_RUN_TIME, config.getMaxStatementsTotal());
    }

    /**
     * Forcibly close a connection and return it to the pool
     *
     * @param id
     * @throws SqlServiceException
     */
    public void forceCloseConnection(int id) throws SqlServiceException {
        boolean found = false;
        // key is the connection, value is the URI in this map
        for (Entry entry : connectionPool.getActiveObjects().entrySet()) {
            DatabaseConnection con = (DatabaseConnection) entry.getKey();
            String url = (String) entry.getValue();
            if (con.getId() == id) {
                getLog().debug("Forcibly closing connection to " + url);
                try {
                    con.setValid(false);
                    connectionPool.returnObject(url, con);
                } catch (Exception e1) {
                    getLog().warn("Could not return connection to uri " + url, e1);
                }
                found = true;
                break;
            }
        }
        if (!found) {
            String msg = "Could not find connection for closure with id " + id;
            getLog().error(msg);
            throw new SqlServiceException(msg, ServiceException.PROCESS_FATAL);
        }
    }

    protected Map<String, URIConnectionInfo> getConnectionInfo() {
        Map<String, URIConnectionInfo> map = CollectionUtils.createHashMap();
        for (Object key : connectionPool.getKeys()) {
            Collection<Object> active = connectionPool.getActiveObjects(key);
            Collection<Object> idle = connectionPool.getIdleObjects(key);
            if (active.size() > 0 || idle.size() > 0) {
                URIConnectionInfo uriInfo = new URIConnectionInfo((String) key);
                map.put((String) key, uriInfo);
                for (Object o : active) {
                    uriInfo.addConnection(new ConnectionInfo((DatabaseConnection) o, true));
                }
                uriInfo.setActiveConnections(active.size());
                for (Object o : idle) {
                    uriInfo.addConnection(new ConnectionInfo((DatabaseConnection) o, false));
                }
                uriInfo.setIdleConnections(idle.size());
            }
        }
        return map;
    }

    public Map<String, URIConnectionInfo> getServiceReport() throws SqlServiceException {
        try {
            Map<String, URIConnectionInfo> map = getConnectionInfo();
            for (Entry<String, URIConnectionInfo> ent : map.entrySet()) {
                String uri = ent.getKey();
                if (uri.matches("jdbc:oracle:thin:.*")) {
                    Connection con = null;
                    try {
                        con = DriverManager.getConnection((String) uri);

                        HashSet<String> activeStatements = new HashSet<String>();

                        String sql = null;
                        ResultSet rs = null;
                        try {
                            sql = "select b.sql_text from v$session a, v$sqlarea b where a.sql_address=b.address and a.username=sys_context( 'userenv', 'session_user' )";
                            Statement st = con.createStatement();
                            rs = st.executeQuery(sql);
                            while (rs.next()) {
                                activeStatements.add(rs.getString(1).replaceAll("\\s+", " "));
                            }
                            st.close();
                            rs.close();
                        } catch (SQLException e) {
                            getLog().warn(
                                    "Could not execute monitor command " + sql + " on " + uri + ":" + e.getMessage());
                        }

                        try {
                            sql = "SELECT SQL_TEXT, count(*) as INSTANCES FROM v$open_cursor o, V$SESSION s "
                                    + "WHERE s.username = sys_context( 'userenv', 'session_user' ) AND  "
                                    + "s.SID = o.SID  GROUP BY SQL_TEXT ORDER BY INSTANCES DESC";
                            Statement st = con.createStatement();
                            rs = st.executeQuery(sql);
                            while (rs.next()) {
                                String stm = rs.getString(1).replaceAll("\\s+", " ");
                                boolean active = false;
                                for (String sta : activeStatements) {
                                    if (sta.startsWith(stm)) {
                                        active = true;
                                        break;
                                    }
                                }
                                ent.getValue().addStatementInfo(new SqlStatementInfo(stm, rs.getInt(2), active));
                            }
                            rs.close();
                            st.close();
                        } catch (SQLException e) {
                            getLog().warn(
                                    "Could not execute monitor command " + sql + " on " + uri + ":" + e.getMessage());
                        }

                        try {
                            sql = "SELECT count(*) AS Cursor , o.SID, o.USER_NAME, s.machine, s.program FROM"
                                    + " v$open_cursor o, V$SESSION s WHERE s.SID = o.SID GROUP by o.SID, o.USER_NAME,s.machine, s.program";
                            Statement st = con.createStatement();
                            rs = st.executeQuery(sql);
                            while (rs.next()) {
                                ent.getValue().addCursorUsage(new CursorUsage(rs.getInt(1), rs.getString(2),
                                        rs.getString(3), rs.getString(4), rs.getString(5)));
                            }
                            rs.close();
                            st.close();
                        } catch (SQLException e) {
                            getLog().warn(
                                    "Could not execute monitor command " + sql + " on " + uri + ":" + e.getMessage());
                        } catch (Throwable e) {
                            getLog().warn(
                                    "Could not execute monitor command " + sql + " on " + uri + ":" + e.getMessage());
                        }
                    } finally {
                        DbUtils.closeDbObject(con);
                    }
                }
            }
            return map;
        } catch (Throwable e) {
            throw new SqlServiceException("Cannot monitor connections for LocalSqlService instance", e,
                    ServiceException.PROCESS_FATAL);
        } finally {
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.services.SqlService#openConnection(java.lang.String)
     */
    public Connection openConnection(String uri) throws SqlServiceException {
        return this.openDatabaseConnection(uri);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.services.impl.SqlService#openConnection(java.lang.
     * String)
     */
    public DatabaseConnection openDatabaseConnection(String uri) throws SqlServiceException {
        try {
            getLog().trace("Opening to " + uri);
            DatabaseConnection con = (DatabaseConnection) (connectionPool.borrowObject(uri));
            return con;
        } catch (Exception e) {
            String msg = "Could not open connection to " + uri;
            getLog().error(msg, e);
            throw new SqlServiceException(msg, e, ServiceException.PROCESS_TRANSIENT);
        }
    }

    /**
     * Returns the connection to the pool provided that its associated result
     * set is closed. Otherwise, the connection must be left open.
     */
    public void releaseConnection(DatabaseConnection con) throws SqlServiceException {
        if (con != null && con.isActive()) {
            if (!con.hasPendingResults()) {
                getLog().debug("Releasing to " + con.getUrl());
                try {
                    connectionPool.returnObject(con.getUrl(), con);
                } catch (Exception e) {
                    throw new SqlServiceException("Could not close connection to " + con.getUrl(), e,
                            ServiceException.PROCESS_FATAL);
                }
            } else {
                getLog().debug("Not releasing to " + con.getUrl() + " as connection still has pending results");
                con.setReadyToClose(true);
            }
        }
    }

    public void releaseConnection(Connection con) throws SqlServiceException {
        if (DatabaseConnection.class.isAssignableFrom(con.getClass())) {
            releaseConnection((DatabaseConnection) con);
        } else {
            throw new SqlServiceException("Cannot release a connection which has not been opened by this service",
                    ServiceException.PROCESS_FATAL);
        }
    }
}
