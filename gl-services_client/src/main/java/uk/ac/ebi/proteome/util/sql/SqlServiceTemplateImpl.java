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

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.services.sql.SqlServiceException;
import uk.ac.ebi.proteome.services.sql.SqlServiceUncheckedException;
import uk.ac.ebi.proteome.util.sql.defaultmappers.DefaultObjectRowMapper;

/**
 * The default implementation of {@link SqlServiceTemplate} which provides all
 * the basic functionality that should be expected from a class which implements
 * this class. Some details of implementation:
 *
 * <ol>
 * <li>The template is hardcoded to one URI as provided to it at construction.
 * This is done as a simplification of using the template</li>
 * </ol>
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class SqlServiceTemplateImpl implements SqlServiceTemplate {

    private final SqlService sqlService;
    private final String uri;
    private Log log = LogFactory.getLog(this.getClass());

    public SqlServiceTemplateImpl(String uri, SqlService sqlService) {
        this.uri = uri;
        this.sqlService = sqlService;
    }

    /**
     * Provides access to the private logger for this class
     */
    protected Log getLog() {
        return log;
    }

    /**
     * {@inheritDoc}
     */
    public String getUri() {
        return uri;
    }

    /**
     * {@inheritDoc}
     */
    public ROResultSet executeSql(String sql, Object... args) throws SqlServiceUncheckedException {
        try {
            return getSqlService().getSqlResult(getUri(), sql, args);
        } catch (SqlServiceException e) {
            throw createUncheckedException(sql, args, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ROResultSet executeSqlNonCached(String sql) throws SqlServiceUncheckedException {
        try {
            return getSqlService().getSqlResult(getUri(), sql, false);
        } catch (SqlServiceException e) {
            throw createUncheckedException(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object[][] executeSqlNonCachedWithObjectReturn(String sql) throws SqlServiceUncheckedException {
        try {
            return getSqlService().executeSql(getUri(), sql);
        } catch (SqlServiceException e) {
            throw createUncheckedException(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object[][] executeSqlWithObjectReturn(String sql, Object... args) throws SqlServiceUncheckedException {
        try {
            return getSqlService().executeSql(getUri(), sql, args);
        } catch (SqlServiceException e) {
            throw createUncheckedException(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ROResultSet executeCall(String sql, Object[] args, int... outputTypes) throws SqlServiceUncheckedException {
        try {
            return getSqlService().getCallResult(getUri(), sql, args, outputTypes);
        } catch (SqlServiceException e) {
            throw createUncheckedException(sql, args, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object[][] executeCallWithObjectArrayOutput(String sql, Object[] args, int... outputTypes)
            throws SqlServiceUncheckedException {
        try {
            return getSqlService().executeCall(getUri(), sql, args, outputTypes);
        } catch (SqlServiceException e) {
            throw createUncheckedException(sql, args, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[] executeTransactionalDml(TransactionalDmlHolder holder) throws SqlServiceUncheckedException {
        try {
            return getSqlService().executeTransactionalDml(getUri(), holder.getStatementsArray(),
                    holder.getParametersArray());
        } catch (SqlServiceException e) {
            throw createUncheckedException(holder, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[] executeNonCachedTransactionalDml(TransactionalDmlHolder holder) throws SqlServiceUncheckedException {
        try {
            return getSqlService().executeTransactionalDml(getUri(), holder.getStatementsArray(),
                    holder.getParametersArray(), false);
        } catch (SqlServiceException e) {
            throw createUncheckedException(holder, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T> List<T> mapROResultSetToList(ROResultSet resultSet, RowMapper<T> mapper, final int rowLimit, String sql,
            Object[] args) throws SqlServiceUncheckedException {
        List<T> output = new ArrayList<T>();
        int position = 0;
        boolean inspectRowCount = (rowLimit > 0);

        try {
            while (resultSet.next()) {

                if (inspectRowCount && position > rowLimit) {
                    String expected = Integer.toString(rowLimit);
                    String actual = Integer.toString(position);
                    String exceptionMessage = MessageFormat.format(
                            "Too many rows returned. " + "Expected {0} but actual row count was {1}",
                            new Object[] { expected, actual });
                    String message = formatExceptionMessage(exceptionMessage, sql, args);
                    throw new SqlServiceUncheckedException(message);
                }

                output.add(mapper.mapRow(resultSet, position));
                position++;
            }

            if (inspectRowCount && position == 0) {
                String message = formatExceptionMessage("Did not find any rows", sql, args);
                throw new SqlServiceUncheckedException(message);
            }
        } catch (SQLException e) {
            String message = formatExceptionMessage("Encountered problem whilst mapping ROResultSet to Object List",
                    sql, args);
            throw new SqlServiceUncheckedException(message, e);
        }

        return output;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T mapROResultSetToSingleObject(ROResultSet resultSet, RowMapper<T> mapper, String sql, Object[] args)
            throws SqlServiceUncheckedException {
        List<T> results = mapROResultSetToList(resultSet, mapper, 1, sql, args);
        return results.get(0);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args) throws SqlServiceUncheckedException {
        ROResultSet resultSet = null;
        try {
            resultSet = executeSql(sql, args);
            T output = mapROResultSetToSingleObject(resultSet, mapper, sql, args);
            return output;
        } finally {
            DbUtils.closeDbObject(resultSet);
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... args)
            throws SqlServiceUncheckedException {
        ROResultSet resultSet = null;
        try {
            resultSet = executeSql(sql, args);
            List<T> output = mapROResultSetToList(resultSet, mapper, -1, sql, args);
            return output;
        } finally {
            DbUtils.closeDbObject(resultSet);
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T> T queryForDefaultObject(String sql, Class<T> expected, Object... args)
            throws SqlServiceUncheckedException {
        DefaultObjectRowMapper<T> mapper = new DefaultObjectRowMapper<T>(expected, 1);
        return queryForObject(sql, mapper, args);
    }

    /**
     * {@inheritDoc}
     */
    public <T> List<T> queryForDefaultObjectList(String sql, Class<T> expected, Object... args)
            throws SqlServiceUncheckedException {
        DefaultObjectRowMapper<T> mapper = new DefaultObjectRowMapper<T>(expected, 1);
        return queryForList(sql, mapper, args);
    }

    /**
     * {@inheritDoc}
     */
    public <K, T> Map<K, T> queryForMap(String sql, MapRowMapper<K, T> mapRowMapper, Object... args)
            throws SqlServiceUncheckedException {

        Map<K, T> targetMap = mapRowMapper.getMap();

        ROResultSet resultSet = null;
        try {
            resultSet = executeSql(sql, args);
            int position = -1;
            while (resultSet.next()) {
                position++;
                K key = mapRowMapper.getKey(resultSet);
                if (targetMap.containsKey(key)) {
                    T currentValue = targetMap.get(key);
                    mapRowMapper.existingObject(currentValue, resultSet, position);
                } else {
                    T newValue = mapRowMapper.mapRow(resultSet, position);
                    targetMap.put(key, newValue);
                }
            }
        } catch (SQLException e) {
            throw new SqlServiceUncheckedException("Cannot map from result set into Map", e);
        } finally {
            DbUtils.closeDbObject(resultSet);
        }

        return targetMap;
    }

    /**
     * {@inheritDoc}
     */
    public int[] executeBatchDml(String sql, BatchDmlHolder holder) throws SqlServiceUncheckedException {
        int[] affectedRows = ArrayUtils.EMPTY_INT_ARRAY;
        try {
            affectedRows = getSqlService().executeBatchDml(getUri(), sql, holder.getAllParams(), holder.getBatchSize());
        } catch (SqlServiceException e) {
            throw createUncheckedException(sql, holder, e);
        }
        return affectedRows;
    }

    // ----- EXCEPTION HANDLING

    /**
     * Used to generically raise unchecked exceptions from sql service
     * exceptions
     */
    private SqlServiceUncheckedException createUncheckedException(String sql, Object[] params, SqlServiceException e) {
        String message = formatExceptionMessage("Could not run statement because of SqlServiceException", sql, params);
        return new SqlServiceUncheckedException(message, e);
    }

    /**
     * Used to generically raise unchecked exceptions from sql service
     * exceptions
     */
    private SqlServiceUncheckedException createUncheckedException(TransactionalDmlHolder holder,
            SqlServiceException e) {
        String message = formatExceptionMessage("Could not run statement because of SqlServiceException", holder);
        return new SqlServiceUncheckedException(message, e);
    }

    /**
     * Used to generically raise unchecked exceptions from sql service
     * exceptions
     */
    private SqlServiceUncheckedException createUncheckedException(String sql, BatchDmlHolder holder,
            SqlServiceException e) {
        String message = formatExceptionMessage("Could not run statement because of SqlServiceException", sql, holder);
        return new SqlServiceUncheckedException(message, e);
    }

    /**
     * Not the best at formatting but attempts to provide a certain level of
     * introspection on the holder object
     */
    private String formatExceptionMessage(String exceptionMessage, TransactionalDmlHolder holder) {
        String sql = Arrays.toString(holder.getStatementsArray());
        return formatExceptionMessage(exceptionMessage, sql, holder.getParametersArray());
    }

    private String formatExceptionMessage(String exceptionMessage, String sql, BatchDmlHolder holder) {
        return formatExceptionMessage(exceptionMessage, sql, holder.getAllParams());
    }

    private int twoDTraceLimit = 3;

    private String formatExceptionMessage(String exceptionMessage, String sql, Object[][] params) {
        List<Object> listArgs = new ArrayList<Object>();

        int loop = (params.length > twoDTraceLimit) ? twoDTraceLimit : params.length;

        for (int i = 0; i < loop; i++) {
            Object[] arg = params[i];
            listArgs.add(Arrays.toString(arg));
        }

        if (params.length > twoDTraceLimit) {
            listArgs.add("More params lines than can show (" + params.length + ") ...");
        }

        Object[] args = listArgs.toArray(new Object[0]);
        return formatExceptionMessage(exceptionMessage, sql, args);
    }

    /**
     * Used to generate the exception messages used through this class
     */
    private String formatExceptionMessage(String exceptionMessage, String sql, Object[] args) {
        String template = "{0} URI => {1} SQL => {2} PARAMS => {3}";
        String paramsString = (ArrayUtils.isEmpty(args)) ? "NONE" : Arrays.toString(args);
        Object[] templateArgs = new Object[] { exceptionMessage, getUri(), sql, paramsString };
        String message = MessageFormat.format(template, templateArgs);
        return message;
    }

    @Override
    public SqlService getSqlService() {
        return this.sqlService;
    }
}
