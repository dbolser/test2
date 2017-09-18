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
 * File: StreamedROResultSet.java
 * Created by: dstaines
 * Created on: Nov 20, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.services.sql.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.services.ServiceException;
import org.ensembl.genomeloader.services.sql.SqlServiceException;

/**
 * @author dstaines
 *
 */
public class StreamedROResultSet extends ObjectArrayROResultSet {

	public boolean hasResults() {
		return stream != null;
	}

	@Override
	public Object[][] getObjects() throws SQLException {
		List<Object[]> objs = new ArrayList<Object[]>();
		while (this.next()) {
			objs.add(row);
		}
		this.close();
		return objs.toArray(new Object[][] {});
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return data;
	}

	protected ObjectInputStream stream;

	private ResultSetMetaData data = null;

	private Log log;

	/**
	 * @param objs
	 */
	public StreamedROResultSet(ObjectInputStream stream, String uri,
			String query) throws SqlServiceException {
		try {
			this.uri = uri;
			this.query = query;
			log = LogFactory.getLog(this.getClass());
			this.stream = stream;
			isClosed = false;
			Object obj = stream.readObject();
			if (obj instanceof Map) {
				Map report = (Map) obj;
				Integer exit = Integer.valueOf(report.get("exit").toString());
				String err = String.valueOf(report.get("error"));
				Throwable exception = (Throwable) report.get("exception");
				if (exception != null)
					throw new SqlServiceException(
							"Exception thrown by remote server when reading result set",
							exception, ServiceException.PROCESS_TRANSIENT);
				if (exit > 0) {
					throw new SqlServiceException(
							"Error returned by remote server when reading result set:"
									+ err, ServiceException.PROCESS_TRANSIENT);
				} else {
					throw new SqlServiceException(
							"Stream from remote streaming SQL server ended unexpectedly with error "
									+ ":" + err, ServiceException.APP_FATAL);
				}
			} else if (obj instanceof ResultSetMetaData) {
				data = (ResultSetMetaData) (obj);
			} else {
				throw new SqlServiceException(
						"Unexpected object found in stream from remote streaming SQL server: "
								+ obj, ServiceException.APP_FATAL);
			}
		} catch (IOException e) {
			throw new SqlServiceException("ResultSetMetaData object not read",
					e, ServiceException.APP_FATAL);
		} catch (ClassNotFoundException e) {
			throw new SqlServiceException("Class ResultSetMetaData not found",
					e, ServiceException.APP_FATAL);
		}
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void afterLast() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void beforeFirst() throws SQLException {
		throw new UnsupportedOperationException();
	}

	protected boolean isClosed = true;

	@Override
	public void close() throws SQLException {
		try {
			if (!isClosed) {
				stream.close();
				log.debug("Stream closed");
				isClosed = true;
			}
		} catch (IOException e) {
			log.debug("Problem closing stream", e);
		} catch (Throwable e) {
			log.debug("Problem closing stream", e);
		}
	}

	@Override
	public boolean first() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFirst() throws SQLException {
		return n == 1;
	}

	@Override
	public boolean isLast() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean last() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean next() throws SQLException {
		try {
			this.row = (Object[]) (stream.readObject());
			if (this.row == null) {
				this.close();
				return false;
			}
			return true;
		} catch (IOException e) {
			log.error("Could not move to next row in stream", e);
			throw new SQLException("Could not move to next row in stream:"
					+ e.getMessage());
		} catch (ClassNotFoundException e) {
			log.error("Could not move to next row in stream", e);
			throw new SQLException("Could not move to next row in stream"
					+ e.getMessage());
		}
	}

	@Override
	public boolean previous() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void streamResult(ObjectOutputStream outputStream)
			throws SQLException, IOException {
		byte[] buf = new byte[1024];
		while (stream.read(buf) != -1) {
			outputStream.write(buf);
		}
		this.close();
	}

}
