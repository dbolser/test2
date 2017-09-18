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

/**
 * File: DatabaseConnection.java
 * Created by: dstaines
 * Created on: Sep 14, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.sql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.services.ServiceException;
import uk.ac.ebi.proteome.services.sql.impl.AbstractConnectionBackedSqlService;
import uk.ac.ebi.proteome.services.sql.impl.JdbcROResultSet;
import uk.ac.ebi.proteome.services.sql.impl.LocalSqlService;
import uk.ac.ebi.proteome.services.sql.impl.ObjectArrayROResultSet;
import uk.ac.ebi.proteome.services.sql.impl.ObjectArrayResultSetMetaData;
import uk.ac.ebi.proteome.util.concurrency.ConcurrencyUtils;
import uk.ac.ebi.proteome.util.sql.DbUtils;
import uk.ac.ebi.proteome.util.sql.RowUtils;

/**
 * Class extending Connection to incorporate pooled prepared statements
 *
 * @author dstaines
 */
public class DatabaseConnection implements Connection {

	// DEBUG public StackTraceElement[] stack = null;

	/**
	 * time in milliseconds before a connection will be abandoned during a close
	 * operation
	 */
	private static final int CLOSE_TIMEOUT = 30000;

	/**
	 * Number of rows to process before resetting the stream cache
	 */
	private static final int CACHE_SIZE = 50;

	/**
	 * Internal counter to give each new connection a unique ID
	 */
	private static int nextId = 0;

	private boolean active = false;

	private int activeSets = 0;

	private Map<String, CallableStatement> calls;

	private Connection connection;

	private int id = nextId++;

	private boolean pendingResults = false;

	private boolean readyToClose = false;

	private ROResultSet resultSet;

	private final AbstractConnectionBackedSqlService srv;

	private Map<String, PreparedStatement> statements;

	private String url;

	private boolean valid = true;

	private Log log;

	private final int statementCacheSize;

	public DatabaseConnection(Connection connection, String url,
			AbstractConnectionBackedSqlService srv, int _statementCacheSize)
			throws SqlServiceException {
		log = LogFactory.getLog(this.getClass());
		this.statementCacheSize = _statementCacheSize;
		this.srv = srv;
		this.connection = connection;
		this.url = url;
		this.statements = new LinkedHashMap<String, PreparedStatement>(
				statementCacheSize + 1, .75F, true) {

			private static final long serialVersionUID = 1L;

			// This method is called just after a new entry has been added
			public boolean removeEldestEntry(
					Map.Entry<String, PreparedStatement> eldest) {
				if (size() > statementCacheSize) {
					DbUtils
							.closeDbObject((PreparedStatement) eldest
									.getValue());
					return true;
				} else {
					return false;
				}
			}
		};
		this.calls = new LinkedHashMap<String, CallableStatement>(
				statementCacheSize + 1, .75F, true) {
			private static final long serialVersionUID = 1L;

			// This method is called just after a new entry has been added
			public boolean removeEldestEntry(
					Map.Entry<String, CallableStatement> eldest) {
				if (size() > statementCacheSize) {
					DbUtils
							.closeDbObject((CallableStatement) eldest
									.getValue());
					return true;
				} else {
					return false;
				}
			}
		};
	}

	public void addSet() {
		activeSets++;
	}

	public void clearResults() {
		if (this.resultSet != null) {
			this.resultSet = null;
		}
	}

	/**
	 * @throws SQLException
	 * @see java.sql.Connection#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		this.connection.clearWarnings();
	}

	/**
	 * @throws SQLException
	 * @see java.sql.Connection#close()
	 */
	@SuppressWarnings("unchecked")
	public void close() throws SQLException {
		if (this.connection != null) {
			try {
				ConcurrencyUtils.executeWithTimeout(CLOSE_TIMEOUT,
						new Callable<Boolean>() {
							public Boolean call() throws Exception {
								for (PreparedStatement s : statements.values()) {
									s.close();
								}
								for (CallableStatement s : calls.values()) {
									s.close();
								}
								connection.close();
								return true;
							}
						});
			} catch (CancellationException e) {
				log.warn("Timed out when trying to close connection to "
						+ this.url);
			} catch (SQLException e) {
				log.warn("Could not close connection to " + this.url);
				throw e;
			} catch (Exception e) {
				log.warn("Failed out when trying to close connection to "
						+ this.url);
			}
		}
	}

	/**
	 * @throws SQLException
	 * @see java.sql.Connection#commit()
	 */
	public void commit() throws SQLException {
		this.connection.commit();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createStatement()
	 */
	public Statement createStatement() throws SQLException {
		return this.connection.createStatement();
	}

	/**
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return this.connection.createStatement(resultSetType,
				resultSetConcurrency);
	}

	/**
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return this.connection.createStatement(resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * Attempts to run the current provided information as a prepared statement
	 * in a batch mode. Only to be used for performing dml statements. As with
	 * all other methods this does not attempt to commit the current connection
	 */
	public int[] executeBatchPreparedStatmentUpdate(String sql,
			Object[][] arguments) throws SQLException {
		int[] updatedRows = ArrayUtils.EMPTY_INT_ARRAY;
		PreparedStatement st = getPreparedStatement(sql);
		for (Object[] args : arguments) {
			bindParamsToPreparedStatement(st, args);
			st.addBatch();
		}
		updatedRows = st.executeBatch();
		return updatedRows;
	}

	public Object[][] executeCall(String sql, Object[] arguments,
			int[] outputTypes) throws SQLException {
		return parseResultSet(getCallResult(sql, arguments, outputTypes));
	}

	public void executeCall(String sql, Object[] arguments, int[] outputTypes,
			ObjectOutputStream stream) throws SQLException, IOException,SqlServiceException {
		streamResultSet(getCallResult(sql, arguments, outputTypes), stream);
	}

	/**
	 * Execute a piece of SQL using the supplied argument, via a cached
	 * statement if possible
	 *
	 * @param sql
	 * @param arguments
	 * @return
	 * @throws SQLException
	 */
	public ROResultSet executePreparedStatement(String sql, Object[] arguments)
			throws SQLException {
		return getSqlResult(sql, arguments);
	}

	/**
	 * Runs the statement as a {@link PreparedStatement#executeUpdate()} method
	 * and returns the number of rows affected by this statement
	 */
	public int executePreparedStatementUpdate(String sql, Object[] arguments,
			boolean cacheStatements) throws SQLException {
		PreparedStatement st = getPreparedStatement(sql, cacheStatements);
		bindParamsToPreparedStatement(st, arguments);
		int n = st.executeUpdate();
		optionalStatementClose(st, cacheStatements);
		return n;
	}

	public Object[][] executeSql(String sql) throws SQLException {
		return parseResultSet(getSqlResult(sql));
	}

	public Object[][] executeSql(String sql, Object[] args) throws SQLException {
		return parseResultSet(getSqlResult(sql, args));
	}

	public void executeSql(String sql, Object[] args, ObjectOutputStream stream)
			throws SQLException, IOException,SqlServiceException {
		streamResultSet(getSqlResult(sql, args), stream);
	}

	public void executeSql(String sql, ObjectOutputStream stream)
			throws SQLException, IOException,SqlServiceException {
		streamResultSet(getSqlResult(sql), stream);
	}

	public int getActiveSets() {
		return activeSets;
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws SQLException {
		return this.connection.getAutoCommit();
	}

	public ROResultSet getCallResult(String sql, Object[] arguments,
			int[] outputTypes) throws SQLException {

		int argumentsLength = (arguments == null) ? 0 : arguments.length;
		int numberOfOutputTypes = (outputTypes == null) ? 0
				: outputTypes.length;

		CallableStatement st = calls.get(sql);
		if (st == null) {
			st = connection.prepareCall(sql);
			registerOutputTypes(st, argumentsLength, outputTypes);
			calls.put(sql, st);
		}

		ROResultSet output = getSqlResult(st, sql, arguments);
		output.setQuery(sql);
		output.setUri(this.url);

		if (numberOfOutputTypes != 0) {
			output.close();
			output = new ObjectArrayROResultSet(retrieveOutputParameters(st,
					argumentsLength, outputTypes), url, sql);
		}

		return output;
	}

	public Map<String, CallableStatement> getCalls() {
		return this.calls;
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getCatalog()
	 */
	public String getCatalog() throws SQLException {
		return this.connection.getCatalog();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		return this.connection.getHoldability();
	}

	public int getId() {
		return this.id;
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getMetaData()
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return this.connection.getMetaData();
	}

	public Map<String, PreparedStatement> getPreparedStatements() {
		return this.statements;
	}

	public ROResultSet getResultSet() {
		return resultSet;
	}

	public ROResultSet getSqlResult(String sql) throws SQLException {
		Statement st = connection.createStatement();
		st.execute(sql);
		resultSet = new JdbcROResultSet(st, this.url, sql, this);
		return resultSet;
	}

	public ROResultSet getSqlResult(String sql, Object[] args)
			throws SQLException {
		ROResultSet rs = getSqlResult(getPreparedStatement(sql), sql, args);
		rs.setUri(this.url);
		rs.setQuery(sql);
		return rs;
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	public int getTransactionIsolation() throws SQLException {
		return this.connection.getTransactionIsolation();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getTypeMap()
	 */
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return this.connection.getTypeMap();
	}

	public String getUrl() {
		return this.url;
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		return this.connection.getWarnings();
	}

	/**
	 * @return true if the attached {@link JdbcROResultSet} is still open
	 */
	public boolean hasPendingResults() {
		return pendingResults;
	}

	public boolean isActive() {
		return this.active;
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		return this.connection.isClosed();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#isReadOnly()
	 */
	public boolean isReadOnly() throws SQLException {
		return this.connection.isReadOnly();
	}

	/**
	 * @return true if client has explicitly called {@link LocalSqlService}.releaseConnection()
	 *         on this connection but it has not completed as results are still
	 *         pending
	 */
	public boolean isReadyToClose() {
		return this.readyToClose;
	}

	public boolean isValid() {
		return this.valid;
	}

	/**
	 * @param sql
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	public String nativeSQL(String sql) throws SQLException {
		return this.connection.nativeSQL(sql);
	}

	/**
	 * @param sql
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		return this.connection.prepareCall(sql);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return this.connection.prepareCall(sql, resultSetType,
				resultSetConcurrency);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return this.connection.prepareCall(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * @param sql
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return this.connection.prepareStatement(sql);
	}

	/**
	 * @param sql
	 * @param autoGeneratedKeys
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return this.connection.prepareStatement(sql, autoGeneratedKeys);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return this.connection.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int,
	 *      int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return this.connection.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * @param sql
	 * @param columnIndexes
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return this.connection.prepareStatement(sql, columnIndexes);
	}

	/**
	 * @param sql
	 * @param columnNames
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String,
	 *      java.lang.String[])
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return this.connection.prepareStatement(sql, columnNames);
	}

	/**
	 * @param savepoint
	 * @throws SQLException
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		this.connection.releaseSavepoint(savepoint);
	}

	public void removeSet() {
		activeSets--;
	}

	/**
	 * Retrieve prepared statement for a given query string
	 *
	 * @param sql
	 * @return
	 */
	public CallableStatement retrieveCall(String sql) {
		return calls.get(sql);
	}

	/**
	 * @throws SQLException
	 * @see java.sql.Connection#rollback()
	 */
	public void rollback() throws SQLException {
		this.connection.rollback();
	}

	/**
	 * @param savepoint
	 * @throws SQLException
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
		this.connection.rollback(savepoint);
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @param autoCommit
	 * @throws SQLException
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		this.connection.setAutoCommit(autoCommit);
	}

	/**
	 * @param catalog
	 * @throws SQLException
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	public void setCatalog(String catalog) throws SQLException {
		this.connection.setCatalog(catalog);
	}

	/**
	 * @param holdability
	 * @throws SQLException
	 * @see java.sql.Connection#setHoldability(int)
	 */
	public void setHoldability(int holdability) throws SQLException {
		this.connection.setHoldability(holdability);
	}

	public void setPendingResults(boolean pending) {
		pendingResults = pending;
	}

	/**
	 * @param readOnly
	 * @throws SQLException
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		this.connection.setReadOnly(readOnly);
	}

	public void setReadyToClose(boolean readyToClose) {
		this.readyToClose = readyToClose;
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#setSavepoint()
	 */
	public Savepoint setSavepoint() throws SQLException {
		return this.connection.setSavepoint();
	}

	/**
	 * @param name
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	public Savepoint setSavepoint(String name) throws SQLException {
		return this.connection.setSavepoint(name);
	}

	/**
	 * @param level
	 * @throws SQLException
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		this.connection.setTransactionIsolation(level);
	}

	/**
	 * @param arg0
	 * @throws SQLException
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		this.connection.setTypeMap(arg0);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * To be used as the generic method of binding parameters out to a prepared
	 * statement
	 */
	private void bindParamsToPreparedStatement(PreparedStatement st,
			Object[] arguments) throws SQLException {
		int i = 0;
		if (arguments != null) {

			for (Object arg : arguments) {
				if (arg == null) {
					st.setNull(++i, Types.NULL);
				} else if (arg instanceof String) {
					st.setString(++i, (String) arg);
				} else if (arg instanceof Integer) {
					st.setInt(++i, (Integer) arg);
				} else if (arg instanceof Boolean) {
					st.setBoolean(++i, (Boolean) arg);
				} else if (arg instanceof Short) {
					st.setShort(++i, (Short) arg);
				} else if (arg instanceof Date) {
					st.setTimestamp(++i, new java.sql.Timestamp(((Date) arg)
							.getTime()));
				} else if (arg instanceof java.sql.Date) {
					st.setDate(++i, new java.sql.Date(((Date) arg).getTime()));
				} else if (arg instanceof Double) {
					st.setDouble(++i, (Double) arg);
				} else if (arg instanceof Long) {
					st.setLong(++i, (Long) arg);
				} else if (arg instanceof BigDecimal) {
					st.setObject(++i, arg);
				} else if (arg instanceof BigInteger) {
					st.setObject(++i, arg);
				} else { // Object
					try {
						ByteArrayOutputStream bytesS = new ByteArrayOutputStream();
						ObjectOutputStream out = new ObjectOutputStream(bytesS);
						out.writeObject(arg);
						out.close();
						byte[] bytes = bytesS.toByteArray();
						bytesS.close();
						st.setBytes(++i, bytes);
					} catch (IOException e) {
						throw new SQLException("Could not serialize object "
								+ arg + " for use in a PreparedStatement ");
					}
				}
			}
		}
	}

	private Object getOutputFromCallableStatement(CallableStatement st,
			int parameterIndex, int type) throws SQLException {
		Object output = null;
		switch (type) {
		case Types.NULL:
			output = null;
			break;
		case Types.BINARY:
		case Types.ARRAY:
		case Types.BLOB:
			output = RowUtils.processBlob(st.getBytes(parameterIndex));
			break;
		case Types.CLOB:
			RowUtils.processClob(st.getClob(parameterIndex));
			break;
		case Types.TIMESTAMP:
			RowUtils.processTimestamp(st.getTimestamp(parameterIndex));
			break;
		case Types.DATE:
			RowUtils.processDate(st.getDate(parameterIndex));
			break;
		case Types.BOOLEAN:
			output = st.getBoolean(parameterIndex);
			break;
		case Types.INTEGER:
			output = st.getInt(parameterIndex);
			break;
		case Types.BIGINT:
			output = st.getLong(parameterIndex);
			break;
		case Types.VARCHAR:
			output = st.getString(parameterIndex);
			break;
		case Types.SMALLINT:
		case Types.BIT:
			output = st.getShort(parameterIndex);
			break;
		case Types.CHAR:
			output = (char) st.getShort(parameterIndex);
			break;
		case Types.NUMERIC:
		case Types.DECIMAL:
			output = st.getBigDecimal(parameterIndex);
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
			output = st.getDouble(parameterIndex);
			break;
		default:
			output = st.getObject(parameterIndex);
			break;
		}

		return output;
	}

	/**
	 * Used to provide a prepared statement which is in a cache pool
	 */
	private PreparedStatement getPreparedStatement(String sql)
			throws SQLException {
		PreparedStatement st = statements.get(sql);
		if (st == null) {
			st = connection.prepareStatement(sql);
			statements.put(sql, st);
		}
		return st;
	}

	/**
	 * Used as a way of shortcutting the optional caching of prepared
	 * statements. If you use the {@link #getPreparedStatement(String)} method
	 * this assumes that you do want a cached statement (default action)
	 */
	private PreparedStatement getPreparedStatement(String sql,
			boolean cacheStatement) throws SQLException {
		PreparedStatement ps = null;
		if (cacheStatement) {
			ps = getPreparedStatement(sql);
		} else {
			ps = connection.prepareStatement(sql);
		}
		return ps;
	}

	/**
	 * Takes in a prepared statment and args and will return a Object[][]
	 * representation of the results returned from the statement's result set
	 */
	private ROResultSet getSqlResult(PreparedStatement st, String sql,
			Object[] arguments) throws SQLException {
		bindParamsToPreparedStatement(st, arguments);
		st.execute();
		resultSet = new JdbcROResultSet(st.getResultSet(), this.url, sql, this);
		return resultSet;
	}

	/**
	 * Closes a statement only if it was not cached
	 */
	private void optionalStatementClose(Statement st, boolean cacheStatement) {
		if (!cacheStatement) {
			DbUtils.closeDbObject(st);
		}
	}

	/**
	 * Extracts the contents of an ROResultSet as a 2d object array and closes
	 * the set
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private Object[][] parseResultSet(ROResultSet rs) throws SQLException {
		try {
			return rs.getObjects();
		} finally {
			rs.close();
		}
	}

	private void registerOutputTypes(CallableStatement st, int offset,
			int[] outputTypes) throws SQLException {
		if (outputTypes != null) {
			int parameterIndex = offset + 1;
			for (int i = 0; i < outputTypes.length; i++) {
				st.registerOutParameter(parameterIndex, outputTypes[i]);
				parameterIndex++;
			}
		}
	}

	private Object[][] retrieveOutputParameters(CallableStatement st,
			int offset, int[] outputTypes) throws SQLException {
		Object[][] output = new Object[1][outputTypes.length];

		int parameterIndex = offset + 1;
		for (int i = 0; i < output[0].length; i++) {
			Object outputObject = getOutputFromCallableStatement(st,
					parameterIndex, outputTypes[i]);
			output[0][i] = outputObject;
			parameterIndex++;
		}

		return output;
	}

	private void flushAndResetStream(final ObjectOutputStream stream)
			throws SqlServiceException {
		try {
			ConcurrencyUtils.executeWithTimeout(CLOSE_TIMEOUT,
					new Callable<Boolean>() {
						public Boolean call() throws Exception {
							stream.flush();
							stream.reset();
							return true;
						}
					});
		} catch (CancellationException e) {
			throw new SqlServiceException("Could not flush and reset stream",
					e, ServiceException.PROCESS_TRANSIENT);
		} catch (Exception e) {
			throw new SqlServiceException("Could not flush and reset stream",
					e, ServiceException.PROCESS_TRANSIENT);
		}

	}

	private void streamResultSet(ROResultSet rs, ObjectOutputStream stream)
			throws SQLException, IOException, SqlServiceException {
		try {
			if (rs != null && rs.hasResults()) {
				ResultSetMetaData data = rs.getMetaData();
				int c = data.getColumnCount();
				int[] types = new int[c];
				for (int i = 0; i < c; i++) {
					types[i] = data.getColumnType(i + 1);
				}
				stream.writeObject(new ObjectArrayResultSetMetaData(types));
				int n = 0;
				while (rs.next()) {
					n++;
					stream.writeObject(RowUtils.parseRow(rs, types));
					if (n % CACHE_SIZE == 0) {
						flushAndResetStream(stream);
					}
				}
			} else {
				stream.writeObject(new ObjectArrayResultSetMetaData());
			}
		} finally {
			if (rs != null)
				rs.close();
			stream.writeObject(null);
			flushAndResetStream(stream);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		if (this.connection != null) {
			close();
		}
		super.finalize();
	}

	public AbstractConnectionBackedSqlService getSqlService() {
		return this.srv;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.connection.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
return this.connection.isWrapperFor(iface);
	}

	public Clob createClob() throws SQLException {
		return this.connection.createClob();
	}

	public Blob createBlob() throws SQLException {
		return this.connection.createBlob();
	}

	public NClob createNClob() throws SQLException {
		return this.connection.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return this.connection.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		return this.connection.isValid(timeout);
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		this.connection.setClientInfo(name, value);
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		this.connection.setClientInfo(properties);
	}

	public String getClientInfo(String name) throws SQLException {
		return this.connection.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		return this.connection.getClientInfo();
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return this.connection.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return this.connection.createStruct(typeName, attributes);
	}

	public void setSchema(String schema) throws SQLException {
		this.connection.setSchema(schema);
	}

	public String getSchema() throws SQLException {
		return this.connection.getSchema();
	}

	public void abort(Executor executor) throws SQLException {
		this.connection.abort(executor);
	}

	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		this.connection.setNetworkTimeout(executor, milliseconds);		
	}

	public int getNetworkTimeout() throws SQLException {
		return this.connection.getNetworkTimeout();
	}

}
