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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.services.ServiceException;
import org.ensembl.genomeloader.services.sql.DatabaseConnection;
import org.ensembl.genomeloader.services.sql.ROResultSet;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.services.sql.SqlServiceException;
import org.ensembl.genomeloader.util.sql.BatchDmlHolder;

/**
 * service class providing access for SQL queries onto RDBMS
 *
 * @author dstaines
 *
 */
public abstract class AbstractConnectionBackedSqlService implements SqlService {

	private Log log = null;

	public static final int DEFAULT_MAX_STATEMENT_CACHE = 20;
	protected int statementCacheSize = 20;
	private static final int[] EMPTY_OUTPUT_TYPES = new int[0];

	/**
	 * Simple constructor which uses default parameters for pool. No
	 * ServiceContext is required
	 */
	public AbstractConnectionBackedSqlService() {
		this(DEFAULT_MAX_STATEMENT_CACHE);
	}

	/**
	 * Directly parameterised constructor for which no ServiceContext is
	 * required. *
	 *
	 * @param statementCacheSize
	 *            number of prepared statements to cache for each connection
	 */
	public AbstractConnectionBackedSqlService(int statementCacheSize) {
		// open a new pool - should parameterise from context
		this.statementCacheSize = statementCacheSize;
	}

	/**
	 * Default implementation of the batch dml statement
	 */
	public int[] executeBatchDml(String uri, String statement, Object[][] args,
			int batchSize) throws SqlServiceException {
		int[] batchAffectedRows = ArrayUtils.EMPTY_INT_ARRAY;

		List<int[]> tempAffectedRows = new ArrayList<int[]>();
		int totalAffectedArrayCount = 0;

		DatabaseConnection conn = null;
		try {
			conn = openDatabaseConnection(uri);
			conn.setAutoCommit(false);
			BatchDmlHolder holder = new BatchDmlHolder(batchSize);
			holder.addParams(args);

			for (Object[][] currentArgs : holder) {
				int[] currentAffectedRows = conn
						.executeBatchPreparedStatmentUpdate(statement,
								currentArgs);
				totalAffectedArrayCount += currentAffectedRows.length;
				tempAffectedRows.add(currentAffectedRows);
			}

			conn.commit();
		} catch (SQLException e) {
			throw new SqlServiceException("Encountered problem whilst working "
					+ "with database " + uri, e,
					ServiceException.PROCESS_TRANSIENT);
		} catch (SqlServiceException e) {
			throw e;
		} finally {
			releaseConnection(conn);
		}

		// Convert the list of affected rows into an int[] array of affected
		// rows
		batchAffectedRows = new int[totalAffectedArrayCount];
		int destPos = 0;
		for (int[] affected : tempAffectedRows) {
			Object src = affected;
			Object dest = batchAffectedRows;
			int srcPos = 0;
			int length = affected.length;
			System.arraycopy(src, srcPos, dest, destPos, length);
			destPos += length;
		}

		return batchAffectedRows;
	}

	public Object[][] executeCall(String uri, String sql, Object[] args)
			throws SqlServiceException {
		return executeCall(uri, sql, args, EMPTY_OUTPUT_TYPES);
	}

	public Object[][] executeCall(String uri, String sql, Object[] args,
			int[] outputTypes) throws SqlServiceException {
		DatabaseConnection con = null;
		try {
			con = this.openDatabaseConnection(uri);
			return con.executeCall(sql, args, outputTypes);
		} catch (SQLException e) {
			throw new SqlServiceException(
					"Could not execute SQL " + sql + " on " + uri
							+ " with arguments " + Arrays.toString(args), e,
					ServiceException.PROCESS_TRANSIENT);
		} finally {
			releaseConnection(con);
		}
	}

	public void executeCall(String uri, String sql, Object[] args,
			int[] outputTypes, ObjectOutputStream stream)
			throws SqlServiceException, IOException {
		DatabaseConnection con = null;
		try {
			con = this.openDatabaseConnection(uri);
			con.executeCall(sql, args, outputTypes, stream);
		} catch (SQLException e) {
			throw new SqlServiceException(
					"Could not execute SQL " + sql + " on " + uri
							+ " with arguments " + Arrays.toString(args), e,
					ServiceException.PROCESS_TRANSIENT);
		} finally {
			releaseConnection(con);
		}
	}

	public void executeCall(String uri, String sql, Object[] args,
			ObjectOutputStream stream) throws SqlServiceException, IOException {
		executeCall(uri, sql, args, EMPTY_OUTPUT_TYPES, stream);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.SqlService#executeSql(java.lang.String,
	 *      java.lang.String)
	 */
	public Object[][] executeSql(String uri, String sql)
			throws SqlServiceException {
		DatabaseConnection con = null;
		try {
			con = this.openDatabaseConnection(uri);
			return con.executeSql(sql);
		} catch (SQLException e) {
			throw new SqlServiceException("Could not execute SQL " + sql
					+ " on " + uri + ":", e, ServiceException.PROCESS_TRANSIENT);
		} finally {
			releaseConnection(con);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.SqlService#executeSql(java.lang.String,
	 *      java.lang.String, boolean)
	 */
	public Object[][] executeSql(String uri, String sql,
			boolean usePreparedStatement) throws SqlServiceException {
		if (usePreparedStatement) {
			return executeSql(uri, sql, new Object[] {});
		} else {
			return executeSql(uri, sql);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.SqlService#executeSql(java.lang.String,
	 *      java.lang.String, boolean, java.io.OutputStream)
	 */
	public void executeSql(String uri, String sql,
			boolean usePreparedStatement, ObjectOutputStream stream)
			throws SqlServiceException, IOException {
		if (usePreparedStatement) {
			executeSql(uri, sql, new Object[] {}, stream);
		} else {
			executeSql(uri, sql, stream);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.SqlService#executeSql(java.lang.String,
	 *      java.lang.String, java.lang.Object[])
	 */
	public Object[][] executeSql(String uri, String sql, Object[] args)
			throws SqlServiceException {
		DatabaseConnection con = null;
		try {
			con = this.openDatabaseConnection(uri);
			return con.executeSql(sql, args);
		} catch (SQLException e) {
			throw new SqlServiceException(
					"Could not execute SQL " + sql + " on " + uri
							+ " with arguments " + Arrays.toString(args), e,
					ServiceException.PROCESS_TRANSIENT);
		} finally {
			releaseConnection(con);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.SqlService#executeSql(java.lang.String,
	 *      java.lang.String, java.lang.Object[], java.io.OutputStream)
	 */
	public void executeSql(String uri, String sql, Object[] args,
			ObjectOutputStream stream) throws SqlServiceException, IOException {
		DatabaseConnection con = null;
		try {
			con = this.openDatabaseConnection(uri);
			con.executeSql(sql, args, stream);
		} catch (SQLException e) {
			throw new SqlServiceException("Could not execute SQL " + sql
					+ " on " + uri + ":", e, ServiceException.PROCESS_TRANSIENT);
		} finally {
			releaseConnection(con);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.SqlService#executeSql(java.lang.String,
	 *      java.lang.String, java.io.OutputStream)
	 */
	public void executeSql(String uri, String sql, ObjectOutputStream stream)
			throws SqlServiceException, IOException {
		DatabaseConnection con = null;
		try {
			con = this.openDatabaseConnection(uri);
			con.executeSql(sql, stream);
		} catch (SQLException e) {
			throw new SqlServiceException("Could not execute SQL " + sql
					+ " on " + uri + ":", e, ServiceException.PROCESS_TRANSIENT);
		} finally {
			releaseConnection(con);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.SqlService#executeTransactionalDml(java.lang.String,
	 *      java.lang.String[], java.lang.Object[][], boolean)
	 */
	public int[] executeTransactionalDml(String uri, String[] statements,
			Object[][] args) throws SqlServiceException {
		return executeTransactionalDml(uri, statements, args, true);
	}

	/**
	 * Provides a method where multiple statemtents can be run under one
	 * transaction. If an exception is detected the transaction is rolled back.
	 * This method should not have Select SQL run through it. Only run update,
	 * delete, insert statements with it.
	 *
	 * @param uri
	 *            The database URI to use to connect to the target DB
	 * @param statements
	 *            The statements which should be all run under the same
	 *            transaction
	 * @param args
	 *            The arguments to provide. Must have one entry in the array per
	 *            statement. If you do not have any arguments for a statement
	 *            you can provide a null or an empty object array. The entire
	 *            array can be null which means that no statements require bind
	 *            params
	 * @return An array of the number of rows updated from each statement.
	 * @throws SqlServiceException
	 */
	public int[] executeTransactionalDml(String uri, String[] statements,
			Object[][] args, boolean cacheStatement) throws SqlServiceException {

		if (statements == null) {
			throw new SqlServiceException("Statement cannot be null",
					ServiceException.PROCESS_TRANSIENT);
		}

		Object[][] newArgs = args;
		if (newArgs == null) {
			newArgs = new Object[statements.length][0];
		}

		if (statements.length != newArgs.length) {
			String msg = MessageFormat
					.format(
							"The input arrays were not "
									+ "of equal length. Statements was {0} and arguments was {1}",
							statements.length, newArgs.length);
			throw new SqlServiceException(msg,
					ServiceException.PROCESS_TRANSIENT);
		}

		int[] results = new int[statements.length];
		DatabaseConnection conn = null;
		try {
			conn = openDatabaseConnection(uri);
			conn.setAutoCommit(false);
			for (int i = 0; i < statements.length; i++) {
				String sql = statements[i];
				if (StringUtils.isEmpty(sql)) {
					throw new SqlServiceException(
							"Recieved empty string for SQL statement. This cannot be",
							ServiceException.PROCESS_TRANSIENT);
				}
				Object[] arguments = newArgs[i];
				int updatedRows = executeUpdate(conn, sql, arguments,
						cacheStatement);
				results[i] = updatedRows;
			}

			conn.commit();
		} catch (SQLException e) {
			throw new SqlServiceException("Encountered problem whilst working "
					+ "with database " + uri, e,
					ServiceException.PROCESS_TRANSIENT);
		} catch (SqlServiceException e) {
			throw e;
		} finally {
			releaseConnection(conn);
		}

		return results;
	}

	/**
	 * Forcibly close a connection and return it to the pool
	 *
	 * @param id
	 * @throws SqlServiceException
	 */
	public abstract void forceCloseConnection(int id)
			throws SqlServiceException;

	public ROResultSet getCallResult(String uri, String sql, Object[] args)
			throws SqlServiceException {
		return getCallResult(uri, sql, args, EMPTY_OUTPUT_TYPES);
	}

	public ROResultSet getCallResult(String uri, String sql, Object[] args,
			int[] outputTypes) throws SqlServiceException {
		DatabaseConnection con = null;
		try {
			con = this.openDatabaseConnection(uri);
			return con.getCallResult(sql, args, outputTypes);
		} catch (SQLException e) {
			throw new SqlServiceException(
					"Could not execute SQL " + sql + " on " + uri
							+ " with arguments " + Arrays.toString(args), e,
					ServiceException.PROCESS_TRANSIENT);
		} finally {
			releaseConnection(con);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.SqlService#getSqlResult(java.lang.String,
	 *      java.lang.String)
	 */
	public ROResultSet getSqlResult(String uri, String sql)
			throws SqlServiceException {
		DatabaseConnection con = null;
		try {
			con = this.openDatabaseConnection(uri);
			return con.getSqlResult(sql);
		} catch (SQLException e) {
			throw new SqlServiceException("Could not execute SQL " + sql
					+ " on " + uri + ":", e, ServiceException.PROCESS_TRANSIENT);
		} finally {
			releaseConnection(con);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.SqlService#getSqlResult(java.lang.String,
	 *      java.lang.String, boolean)
	 */
	public ROResultSet getSqlResult(String uri, String sql,
			boolean usePreparedStatement) throws SqlServiceException {
		if (usePreparedStatement) {
			return getSqlResult(uri, sql, new Object[] {});
		} else {
			return getSqlResult(uri, sql);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.SqlService#getSqlResult(java.lang.String,
	 *      java.lang.String, java.lang.Object[])
	 */
	public ROResultSet getSqlResult(String uri, String sql, Object[] args)
			throws SqlServiceException {
		DatabaseConnection con = null;
		try {
			con = this.openDatabaseConnection(uri);
			return con.getSqlResult(sql, args);
		} catch (SQLException e) {
			throw new SqlServiceException("Could not execute SQL " + sql
					+ " on " + uri + ":", e, ServiceException.PROCESS_TRANSIENT);
		} finally {
			releaseConnection(con);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.SqlService#openConnection(java.lang.String)
	 */
	public Connection openConnection(String uri) throws SqlServiceException {
		return this.openDatabaseConnection(uri);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.impl.SqlService#openConnection(java.lang.String)
	 */
	public abstract DatabaseConnection openDatabaseConnection(String uri)
			throws SqlServiceException;

	public abstract void releaseConnection(DatabaseConnection con)
			throws SqlServiceException;

	public void releaseConnection(Connection con) throws SqlServiceException {
		if (DatabaseConnection.class.isAssignableFrom(con.getClass())) {
			releaseConnection((DatabaseConnection) con);
		} else {
			throw new SqlServiceException(
					"Cannot release a connection which has not been opened by this service",
					ServiceException.PROCESS_FATAL);
		}
	}

	/**
	 * Runs the SQL through
	 * {@link DatabaseConnection#executePreparedStatementUpdate(String, Object[])}
	 * and provides a single query point. It's main function is to be called by
	 * {@link #executeTransactionalDml(String, String[], Object[][])}
	 *
	 * <p>
	 * This method can rollback
	 */
	private int executeUpdate(DatabaseConnection conn, String sql,
			Object[] args, boolean cacheStatements) throws SqlServiceException {

		int updatedRows = -1;

		try {
			updatedRows = conn.executePreparedStatementUpdate(sql, args,
					cacheStatements);
		} catch (SQLException e) {
			String paramsString = Arrays.toString(args);
			String msg = MessageFormat.format("Encountered problem whilst "
					+ "running SQL {0} with params {1}", sql, paramsString);
			throw new SqlServiceException(msg, e,
					ServiceException.PROCESS_TRANSIENT);
		}

		return updatedRows;
	}

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}
}
