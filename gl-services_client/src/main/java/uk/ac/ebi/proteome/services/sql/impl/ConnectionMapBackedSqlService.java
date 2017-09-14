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
 * File: ConnectionMapBackedSqlService.java
 * Created by: dstaines
 * Created on: May 1, 2009
 * CVS:  $$
 */
package uk.ac.ebi.proteome.services.sql.impl;

import java.sql.Connection;
import java.util.Map;

import uk.ac.ebi.proteome.services.ServiceException;
import uk.ac.ebi.proteome.services.sql.DatabaseConnection;
import uk.ac.ebi.proteome.services.sql.SqlServiceException;
import uk.ac.ebi.proteome.services.sql.SqlServiceUncheckedException;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Simple implementation for using existing connections. <strong>Not thread safe</strong>
 *
 * @author dstaines
 *
 */
public class ConnectionMapBackedSqlService extends
		AbstractConnectionBackedSqlService {

	public ConnectionMapBackedSqlService(String uri, Connection con) {
		try {
			connectionMap = buildMap(uri, con);
		} catch (SqlServiceException e) {
			throw new SqlServiceUncheckedException(
					"Could not construct connection-backed service", e);
		}
	}

	private Map<String, DatabaseConnection> connectionMap;

	public ConnectionMapBackedSqlService(Map<String, DatabaseConnection> map) {
		this.connectionMap = map;
	}

	protected Map<String, DatabaseConnection> getConnectionMap() {
		return connectionMap;
	}

	protected Map<String, DatabaseConnection> buildMap(String uri,
			Connection con) throws SqlServiceException {
		Map<String, DatabaseConnection> map = CollectionUtils.createHashMap();
		map.put(uri, new DatabaseConnection(con, uri, this,
				super.statementCacheSize));
		return map;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.services.sql.impl.AbstractConnectionBackedSqlService#forceCloseConnection(int)
	 */
	@Override
	public void forceCloseConnection(int id) throws SqlServiceException {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.services.sql.impl.AbstractConnectionBackedSqlService#openDatabaseConnection(java.lang.String)
	 */
	@Override
	public DatabaseConnection openDatabaseConnection(String uri)
			throws SqlServiceException {
		if (!getConnectionMap().containsKey(uri)) {
			throw new SqlServiceException("Connection with URI " + uri
					+ " not found", ServiceException.PROCESS_FATAL);
		}
		return getConnectionMap().get(uri);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.services.sql.impl.AbstractConnectionBackedSqlService#releaseConnection(uk.ac.ebi.proteome.services.sql.DatabaseConnection)
	 */
	@Override
	public void releaseConnection(DatabaseConnection con)
			throws SqlServiceException {
		// do nothing
	}

}
