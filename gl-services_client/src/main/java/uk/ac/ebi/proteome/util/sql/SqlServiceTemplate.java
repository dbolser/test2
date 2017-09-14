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

package uk.ac.ebi.proteome.util.sql;

import java.util.List;
import java.util.Map;

import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.services.sql.SqlServiceUncheckedException;
import uk.ac.ebi.proteome.util.sql.defaultmappers.DefaultObjectRowMapper;

/**
 * A unashamed copy of Spring's JdbcTemplate code (docs are available from <a
 * href="http://www.springframework.org/docs/api/org/springframework/jdbc/core/JdbcTemplate.html">
 * Spring's Javadoc site</a>. The JdbcTemplate attempted to provide easy access
 * to JDBC functionality whilst not actually hiding any of the core features of
 * JDBC. In a similar way this class provides easy access to the SqlService
 * whilst attempting to provide shortcuts to commonly requested features. If
 * direct access to SqlService is required then this can be retrieved via
 * {@link #getSqlService()}. However for most cases the provided functionality
 * should be more than enough; and if it is not then add it to this class.
 *
 * <p>
 * The object attempts to use as many new features from Java5. This means that
 * some methods will use generics, others will accept the vargs construct. Not
 * all methods will accept them because of the problems with auto-boxing &
 * continuation of arrays. Javadoc will indicate when vargs is being using
 *
 * <p>
 * As a convenience this class exposes it's own wrapper version of the
 * SqlService methods however they all throw runtime based exceptions. These
 * exceptions will report as much about the given SQL as they possibly can. The
 * SQL service will also provide a certain level so when using this class it
 * should not be a requirement to explictly describe the incoming SQL. This
 * class will do it all for you.
 *
 * <p>
 * The methods in this class attempt to provide easy access to retrieve single/
 * multiple objects via the RowMapper pattern. When you wish to perform more
 * complex mappings then please use
 * {@link #queryForList(String, RowMapper, Object[])} or
 * {@link #queryForObject(String, RowMapper, Object[])}.
 *
 * <p>
 * Most of the time you probably will be using the
 * {@link #queryForDefaultObject(String, Class, Object[])} and
 * {@link #queryForDefaultObjectList(String, Class, Object[])} methods. Both of
 * these have example usage in their Javadoc. The methods use the
 * {@link DefaultObjectRowMapper} object to map the result of column 1 for any
 * query given to it. Please consult {@link DefaultObjectRowMapper} for the
 * available Objects to map.
 *
 * <p>
 * There is also the {@link #queryForMap(String, MapRowMapper, Object[])} which
 * can be used to translate a result set into a Map of results. This is very
 * useful for grouping outer joins by a single key for easy lookup. This is used
 * in conjunction with {@link MapRowMapper} which defines callbacks for the
 * lifecycle of the {@link #queryForMap(String, MapRowMapper, Object[])} method.
 *
 * <p>
 * This class should be used in conjunction with {@link DbUtils} and the default
 * implementation {@link SqlServiceTemplateImpl}.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public interface SqlServiceTemplate {

	SqlService getSqlService();

	/**
	 * The currently configured URI to which all SQL will be sent to
	 */
	String getUri();

	/**
	 * Runs a SQL statement against {@link #getUri()}
	 *
	 * @param sql
	 *          The SQL to run
	 * @param args
	 *          Vargs to specify the list of arguments
	 */
	ROResultSet executeSql(String sql, Object... args)
			throws SqlServiceUncheckedException;

	/**
	 * Runs a SQL statement against {@link #getUri()} but ensures that
	 * PreparedStatements will not be used. Use this when working with DDL code
	 */
	ROResultSet executeSqlNonCached(String sql)
			throws SqlServiceUncheckedException;

	/**
	 * Should be used when the executing SQL's output may be ignored. A good
	 * example of this is DDL when you just want it to execute & the detection of
	 * an exception is good enough to communicate that the DDL is unsuccessful.
	 *
	 * <P>
	 * Whilst similar to {@link #executeSqlNonCachedStatement(String)} this method
	 * returns an untyped 2D Object array. The other method would force you to
	 * explicitly close the result set to avoid a resource leak where as this
	 * version will close the resources as specified in the SQL Service.
	 */
	Object[][] executeSqlNonCachedWithObjectReturn(String sql)
			throws SqlServiceUncheckedException;

	/**
	 * Can be used when a prepared statement is required however the output of the
	 * statement is irrelevant or it is more useful to process it as a 2D Object
	 * array.
	 */
	Object[][] executeSqlWithObjectReturn(String sql, Object... args)
			throws SqlServiceUncheckedException;

	/**
	 * Runs a SQL callable statement against {@link #getUri()} and allows the user
	 * to register output parameters. These outputs can be retrieved via the
	 * result set
	 *
	 * @param sql
	 *          The SQL to run
	 * @param args
	 *          The arguments array which must be a hardcoded array because of
	 *          autoboxing problems
	 * @param outputTypes
	 *          Vargs output types which we expect to come back from the callable
	 *          statement
	 */
	ROResultSet executeCall(String sql, Object[] args, int... outputTypes)
			throws SqlServiceUncheckedException;

	/**
	 * Rather than returning a ROResultSet this method returns an Object array
	 * which should make the procedure of working with callable statements and not
	 * wanting to work with resource cleanup easier.
	 */
	Object[][] executeCallWithObjectArrayOutput(String sql, Object[] args,
			int... outputTypes) throws SqlServiceUncheckedException;

	/**
	 * Runs a set of SQL statements against {@link #getUri()} in a transactional
	 * block
	 */
	int[] executeTransactionalDml(TransactionalDmlHolder holder)
			throws SqlServiceUncheckedException;

	/**
	 * The same as {@link #executeTransactionalDml(TransactionalDmlHolder)} but
	 * does not cache the given statements
	 */
	int[] executeNonCachedTransactionalDml(TransactionalDmlHolder holder)
			throws SqlServiceUncheckedException;

	/**
	 * The core method which takes the output of a ROResultSet and will output a
	 * List of objects. This method provides a very useful manner to parse the
	 * outputs of result sets however it is recommended that you use a more custom
	 * method for the procedure.
	 *
	 * @param <T>
	 *          The required output type
	 * @param resultSet
	 *          The input result set
	 * @param mapper
	 *          The mapper object to use to map from result set to object
	 * @param rowLimit
	 *          Indicates that there is an expected row limit that when exceeded
	 *          we want a runtime exception raised. If set to -1 or 0 this is
	 *          ignored. If set then exceeding the row limit or a return count of
	 *          0 will cause an exception to be raised
	 * @param sql
	 *          The SQL used to execute this statement. Used for error reporting
	 * @param args
	 *          The args used to execute this statement. Used for error reporting
	 * @return A list of objects which were created by the mapper
	 * @throws SqlServiceUncheckedException
	 *           Thrown if SQLExceptions were raised during the mapping process or
	 *           row limit was exceeded
	 */
	<T> List<T> mapROResultSetToList(ROResultSet resultSet, RowMapper<T> mapper,
			final int rowLimit, String sql, Object[] args)
			throws SqlServiceUncheckedException;

	/**
	 * Wrapper version for
	 * {@link #mapROResultSetToList(ROResultSet, RowMapper, int, String, Object[])}
	 * which returns a single object
	 */
	<T> T mapROResultSetToSingleObject(ROResultSet resultSet,
			RowMapper<T> mapper, String sql, Object[] args)
			throws SqlServiceUncheckedException;

	/**
	 * Used to call both the {@link #executeSql(String, Object[])} and then call
	 * out to
	 * {@link #mapROResultSetToSingleObject(ROResultSet, RowMapper, String, Object[])}.
	 * This also means that this method will deal with resource handling
	 * correctly.
	 *
	 * @param <T>
	 *          The input type param
	 * @param sql
	 *          The SQL to run
	 * @param mapper
	 *          The mapper to use
	 * @param args
	 *          The arguments for the SQL
	 * @return A single object returned from the given SQL query
	 */
	<T> T queryForObject(String sql, RowMapper<T> mapper, Object... args)
			throws SqlServiceUncheckedException;

	/**
	 * Runs {@link #executeSql(String, Object[])} and then call out to
	 * {@link #mapROResultSetToList(ROResultSet, RowMapper, int, String, Object[])}
	 * for processing into a list.
	 *
	 * @param <T>
	 *          The expected return type
	 * @param sql
	 *          The SQL to execute
	 * @param mapper
	 *          The mapper to use
	 * @param args
	 *          Arguments to use in the SQL
	 * @return The list of specified objects
	 */
	<T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... args)
			throws SqlServiceUncheckedException;

	/**
	 * See {@link DefaultObjectRowMapper} for more information about supported
	 * mappings. Will map column 1 from a result set into a given object. This
	 * method assumes that you always expect one result back from the database and
	 * to recieve more or less than this is an erronious situation. Example usage:
	 *
	 * <code>
	 * SqlServiceTemplate template = getTemplate(); //Resolved from somewhere
	 * int count = template.queryForDefaultObject("select count(*) from person", Integer.class);
	 * </code>
	 *
	 * In the above example we have queried for a count which we know must exist
	 * and will only return one value. We tell the method that we are going to be
	 * querying for an Integer object and that this will be autoboxed to an int.
	 * Since the method relies heavily on Generics this will deal with the
	 * problems of casting & conversion of result to specified data type.
	 *
	 * @throws SqlServiceUncheckedException
	 *           Thrown if the given query brings back less than or more than 1
	 *           result.
	 */
	<T> T queryForDefaultObject(String sql, Class<T> expected, Object... args)
			throws SqlServiceUncheckedException;

	/**
	 * See {@link DefaultObjectRowMapper} for more information about supported
	 * mappings. Will map column 1 from a result set into a given object. Example
	 * usage:
	 *
	 * <code>
	 * SqlServiceTemplate template = getTemplate(); //Resolved from somewhere
	 * List&lt;Date&gt; = template.queryForDefaultObjectList("select dates from date_table", Date.class);
	 * </code>
	 *
	 * In the above example we are querying for all dates from a specified table.
	 * The list will never be null but maybe empty if no results were found.
	 */
	<T> List<T> queryForDefaultObjectList(String sql, Class<T> expected,
			Object... args) throws SqlServiceUncheckedException;

	/**
	 * Provides very similar functionality to
	 * {@link #queryForList(String, uk.ac.ebi.proteome.util.sql.RowMapper, Object[])}
	 * however this assumes that there is a difference in the expected mapping
	 * from results to domain object. You use {@link MapRowMapper} objects to help
	 * this method to decode from the ResultSet into a Map. The procedure run is
	 *
	 * <ol>
	 * <li>Get the map to populate from {@link MapRowMapper#getMap()}</li>
	 * <li>Run the query with the given arguments</li>
	 * <li>Iterate through the results set</li>
	 * <li>Call {@link MapRowMapper#getKey(ROResultSet)} with the results</li>
	 * <li>Query against the map to see if the key has already been seen or not</li>
	 * <ol>
	 * <li>If it has not then call {@link RowMapper#mapRow(ROResultSet, int)}</li>
	 * <li>If it has then call
	 * {@link MapRowMapper#existingObject(Object, ROResultSet, int)} and pass back
	 * the Object associcated with the key</li>
	 * </ol>
	 * <li>Repeat until the result set is finished</li>
	 * <li>Return the generated map</li>
	 * </ol>
	 *
	 * Because you are given such control over what happens when this method runs
	 * the generated map can be anything, you can throw exceptions if you
	 * encounter more than one instance of the key or just add it to a Java
	 * collection.
	 *
	 * @param <K>
	 *          The target key type
	 * @param <T>
	 *          The target value type
	 * @param sql
	 *          The SQL to run to generate this
	 * @param mapRowMapper
	 *          The instance of the row mapper
	 * @param args
	 *          Arguments to send to the target server
	 * @return A map which should be of the given above type
	 * @throws SqlServiceUncheckedException
	 *           Thrown in the event of problems with the mappings
	 */
	<K, T> Map<K, T> queryForMap(String sql, MapRowMapper<K, T> mapRowMapper,
			Object... args) throws SqlServiceUncheckedException;

	/**
	 * Thin wrapper around the
	 * {@link SqlService#executeBatchDml(String, String, Object[][], int)} method
	 * where all information about batching apart from the SQL to run is in the
	 * {@link BatchDmlHolder} object. When performing large scale DML operations
	 * this is by far the quickest way to perform them (apart from inserting when
	 * working with External tables or Sql*Loader).
	 *
	 * @param sql
	 *          The SQL to run in batch
	 * @param holder
	 *          The holder of the batch arguments
	 * @return An int[] detailing the current output from the batch
	 * @throws SqlServiceUncheckedException
	 *           Thrown if there is a problem during the running of the batch
	 */
	int[] executeBatchDml(String sql, BatchDmlHolder holder)
			throws SqlServiceUncheckedException;

}
