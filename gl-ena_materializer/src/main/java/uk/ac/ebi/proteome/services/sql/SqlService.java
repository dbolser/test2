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
 * CVS:  $Id$
 * File: SqlService.java
 * Author: dstaines
 * Created on: Jul 27, 2006
 *
 */
package uk.ac.ebi.proteome.services.sql;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author dstaines
 *
 */
public interface SqlService {

	/**
	 * Execute the SQL on the specified URI using the prepared statement cache
	 *
	 * @param uri
	 * @param sql
	 * @param args
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract Object[][] executeSql(String uri, String sql, Object[] args)
			throws SqlServiceException;

	/**
	 * Execute the SQL on the specified URI, optionally using the prepared
	 * statement cache
	 *
	 * @param uri
	 * @param sql
	 * @param usePreparedStatement
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract Object[][] executeSql(String uri, String sql,
			boolean usePreparedStatement) throws SqlServiceException;

	/**
	 * Execute the SQL on the specified URI using a simple statement
	 *
	 * @param uri
	 * @param sql
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract Object[][] executeSql(String uri, String sql)
			throws SqlServiceException;

	/**
	 * Execute the SQL on the specified URI using the prepared statement cache
	 *
	 * @param uri
	 * @param sql
	 * @param args
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract ROResultSet getSqlResult(String uri, String sql,
			Object[] args) throws SqlServiceException;

	/**
	 * Execute the SQL on the specified URI, optionally using the prepared
	 * statement cache
	 *
	 * @param uri
	 * @param sql
	 * @param usePreparedStatement
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract ROResultSet getSqlResult(String uri, String sql,
			boolean usePreparedStatement) throws SqlServiceException;

	/**
	 * Execute the SQL on the specified URI using a simple statement
	 *
	 * @param uri
	 * @param sql
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract ROResultSet getSqlResult(String uri, String sql)
			throws SqlServiceException;

	/**
	 * Execute the SQL on the specified URI using the prepared statement cache
	 *
	 * @param uri
	 * @param sql
	 * @param args
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract void executeSql(String uri, String sql, Object[] args,
			ObjectOutputStream stream) throws SqlServiceException, IOException;

	/**
	 * Execute the SQL on the specified URI, optionally using the prepared
	 * statement cache
	 *
	 * @param uri
	 * @param sql
	 * @param usePreparedStatement
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract void executeSql(String uri, String sql,
			boolean usePreparedStatement, ObjectOutputStream stream)
			throws SqlServiceException, IOException;

	/**
	 * Execute the SQL on the specified URI using a simple statement
	 *
	 * @param uri
	 * @param sql
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract void executeSql(String uri, String sql,
			ObjectOutputStream stream) throws SqlServiceException, IOException;

	/**
	 * Execute the SQL on the specified URI, using the callable statement cache
	 *
	 * @param uri
	 * @param sql
	 * @param args
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract Object[][] executeCall(String uri, String sql, Object[] args)
			throws SqlServiceException;

	/**
	 * Execute the SQL on the specified URI, using the callable statement cache
	 *
	 * @param uri
	 *            of target database
	 * @param sql
	 *            to execute
	 * @param args
	 *            for SQL
	 * @param stream
	 * @return
	 * @throws SqlServiceException
	 */
	public abstract void executeCall(String uri, String sql, Object[] args,
			ObjectOutputStream stream) throws SqlServiceException, IOException;

	/**
	 * Execute the SQL on the specified URI, using the callable statement cache
	 *
	 * @param uri
	 *            of target database
	 * @param sql
	 *            to execute
	 * @param args
	 *            for SQL
	 * @return ROResultSet
	 * @throws SqlServiceException
	 */
	public abstract ROResultSet getCallResult(String uri, String sql,
			Object[] args) throws SqlServiceException;

	/**
	 * Execute the SQL on the specified URI, using the callable statement cache
	 * and registering output parameters for the callable statement
	 *
	 * @param uri
	 *            The location of the target DB
	 * @param sql
	 *            The SQL to call (should be a reference to a call on the server
	 *            or a inline call)
	 * @param args
	 *            The arguments to set in the statement
	 * @param outputTypes
	 *            Any types that are to be returned from this statement
	 * @return A 2D array. If any types were specified to be returned then these
	 *         will replace the contents of any result set generated by the
	 *         statement
	 * @throws SqlServiceException
	 */
	public abstract Object[][] executeCall(String uri, String sql,
			Object[] args, int[] outputTypes) throws SqlServiceException;

	/**
	 * Execute the SQL on the specified URI, using the callable statement cache
	 * and registering output parameters for the callable statement, writing the
	 * result to the supplied stream
	 *
	 * @param uri
	 *            The location of the target DB
	 * @param sql
	 *            The SQL to call (should be a reference to a call on the server
	 *            or a inline call)
	 * @param args
	 *            The arguments to set in the statement
	 * @param outputTypes
	 *            Any types that are to be returned from this statement
	 * @param stream
	 *            output stream to write to
	 * @throws SqlServiceException
	 * @throws IOException
	 */
	public abstract void executeCall(String uri, String sql, Object[] args,
			int[] outputTypes, ObjectOutputStream stream)
			throws SqlServiceException, IOException;

	/**
	 * Execute the SQL on the specified URI, using the callable statement cache
	 * and registering output parameters for the callable statement and return a
	 * ROResultSet
	 *
	 * @param uri
	 *            The location of the target DB
	 * @param sql
	 *            The SQL to call (should be a reference to a call on the server
	 *            or a inline call)
	 * @param args
	 *            The arguments to set in the statement
	 * @param outputTypes
	 *            Any types that are to be returned from this statement
	 * @throws SqlServiceException
	 * @return
	 */
	public abstract ROResultSet getCallResult(String uri, String sql,
			Object[] args, int[] outputTypes) throws SqlServiceException;

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
	int[] executeTransactionalDml(String uri, String[] statements,
			Object[][] args) throws SqlServiceException;

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
	 * @param cacheStatements
	 *            whether to cache the underlying prepared statement
	 * @return An array of the number of rows updated from each statement.
	 * @throws SqlServiceException
	 */
	int[] executeTransactionalDml(String uri, String[] statements,
			Object[][] args, boolean cacheStatements) throws SqlServiceException;

	/**
	 * Performs batch updates on the given array of arguments. It assumes that
	 * the provided arguments array is the full extent of the current batch
	 * to be inserted. You must specify the size of the batch statements &
	 * be aware of the limitations of working with large scale inserts & memory
	 * issues.
	 *
	 * @param uri
	 * @param statement
	 * @param args
	 * @param batchSize
	 * @return
	 * @throws SqlServiceException
	 */
	int[] executeBatchDml(String uri, String statement, Object[][] args, int batchSize) throws SqlServiceException;

}
