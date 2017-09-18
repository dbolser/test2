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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.services.ServiceUncheckedException;
import uk.ac.ebi.proteome.util.PropertyUtils;

/**
 * Intended as a temporary measure until we can get a hold of DbCon
 * 
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class SqlStatementRetriever {

	private static Log log = LogFactory.getLog(SqlStatementRetriever.class);

	private static Log getLog() {
		return log;
	}

	private static final Object LOCK = new Object();

	/**
	 * Private static synchronized map used to store libraries in
	 */
	private static Map<SqlLibrary, Map<String, String>> libraries = Collections
			.synchronizedMap(new HashMap<SqlLibrary, Map<String, String>>());

	/**
	 * Performs checks on SQL gets output and fails fast if there is a problem
	 */
	public static String getSql(SqlLibrary library, String sqlName,
			Object... params) {
		String sql = getLibrary(library).get(sqlName);

		if (StringUtils.isBlank(sql)) {
			String msg = "Sql name {0} against library {1} produced a blank String "
					+ "according to the rules found in commons-lang's "
					+ "StringUtils.isBlank() method";
			throw new ServiceUncheckedException(MessageFormat.format(msg,
					sqlName, library.getSqlLocation()));
		}

		// Replace with the params output
		sql = processSql(sql, params);

		return sql;
	}

	/**
	 * Currently sets the given parameters into the SQL statement which has
	 * placeholders in the format specified by {@link MessageFormat}. This can
	 * be changed to other formats or methods as required
	 * 
	 * @param sql
	 *            The sql statement to bind to
	 * @param params
	 *            The parameters to bind to the SQL statement
	 * @return The full bound version of the statement
	 */
	public static String processSql(String sql, Object... params) {
		String processedSql = null;

		if (params.length == 0) {
			processedSql = sql;
		} else {
			processedSql = MessageFormat.format(sql, params);
		}

		if (getLog().isDebugEnabled()) {
			getLog().debug("Generated SQL: " + processedSql);
		}

		return processedSql;
	}

	/**
	 * Gets the library for the specified object and will retrieve instances
	 * from the static hashmap
	 */
	public static Map<String, String> getLibrary(SqlLibrary library) {
		Map<String, String> output = libraries.get(library);

		synchronized (LOCK) {
			if (output == null) {
				output = loadLibrary(library);
				libraries.put(library, output);
			}
		}

		return output;
	}

	private static Map<String, String> loadLibrary(SqlLibrary library) {
		Map<String, String> output = new HashMap<String, String>();
		Properties props = getProperties(library);
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			output.put((String) entry.getKey(), (String) entry.getValue());
		}

		return output;
	}

	private static Properties getProperties(SqlLibrary library) {
		String location = library.getSqlLocation();
		return PropertyUtils.getProperties(location);
	}
}
