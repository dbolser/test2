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
 * File: JdbcROResultSet.java
 * Created by: dstaines
 * Created on: Nov 16, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.sql.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.proteome.services.ServiceUncheckedException;
import uk.ac.ebi.proteome.services.sql.DatabaseConnection;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.util.InputOutputUtils;
import uk.ac.ebi.proteome.util.sql.RowUtils;

/**
 * @author dstaines
 *
 */
public class JdbcROResultSet implements ROResultSet {

	private boolean closed = false;

	public boolean isClosed() {
		return this.closed;
	}

	public boolean hasResults() {
		return set != null;
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	private final ResultSet set;

	private final Statement statement;

	private final DatabaseConnection con;

	/**
	 * Constructor used when non-cached statement is used. The statement is
	 * automatically closed when the set is closed
	 *
	 * @param st
	 * @param uri
	 * @param query
	 * @param con
	 * @param srv
	 * @throws SQLException
	 */
	public JdbcROResultSet(Statement st, String uri, String query,
			DatabaseConnection con) throws SQLException {
		con.setPendingResults(true);
		this.uri = uri;
		this.query = query;
		closed = false;
		this.set = st.getResultSet();
		this.statement = st;
		this.con = con;
	}

	/**
	 * Constructor used when cached statement is used to generate a result set.
	 * The statement is not passed in and hence is not closed here
	 *
	 * @param st
	 * @param uri
	 * @param query
	 * @param con
	 * @param srv
	 * @throws SQLException
	 */
	public JdbcROResultSet(ResultSet set, String uri, String query,
			DatabaseConnection con) {
		con.setPendingResults(true);
		this.uri = uri;
		this.query = query;
		closed = false;
		this.set = set;
		this.con = con;
		this.statement = null;
	}

	public boolean absolute(int row) throws SQLException {
		return hasResults() && this.set.absolute(row);
	}

	public void afterLast() throws SQLException {
		if (hasResults())
			this.set.afterLast();
	}

	public void beforeFirst() throws SQLException {
		if (hasResults())
			this.set.beforeFirst();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.services.sql.ROResultSet#close()
	 */
	public void close() throws SQLException {
		// if the set has not already been closed, we need to close it and also
		// make sure the connection gets closed, if appropriate
		if (!closed) {
			try {
				// first, try to close the underlying ResultSet
				try {
					if (this.set != null)
						this.set.close();
				} catch (SQLException e) {
					// dont worry if this fails at close time
				}
				try {
					if (this.statement != null) {
						this.statement.close();
					}
				} catch (SQLException e) {
					// dont worry if this fails at close time
				}
			} finally {
				// mark the ROResultSet as closed
				closed = true;
				// mark the connection as
				con.setPendingResults(false);
				// if the connection has already been marked as ready to close,
				// we need to close it. This would have happened if another
				// class tried to release the connection but nothing happened
				// since this set was still pending
				if (this.equals(con.getResultSet()) && con.isReadyToClose()) {
					try {
						// release the connection back to the pool
						con.getSqlService().releaseConnection(con);
					} catch (Exception e) {
						throw new ServiceUncheckedException(
								"Could not close connection to " + con.getUrl(),
								e);
					}
				}
			}
		}
	}

	public void deleteRow() throws SQLException {
		if (hasResults())
			this.set.deleteRow();
	}

	public boolean first() throws SQLException {
		return this.set.first();
	}

	public Array getArray(int i) throws SQLException {
		return this.set.getArray(i);
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return this.set.getAsciiStream(columnIndex);
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return this.set.getBigDecimal(columnIndex);
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return this.set.getBinaryStream(columnIndex);
	}

	public Blob getBlob(int i) throws SQLException {
		return this.set.getBlob(i);
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		return this.set.getBoolean(columnIndex);
	}

	public byte getByte(int columnIndex) throws SQLException {
		return this.set.getByte(columnIndex);
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		return this.set.getBytes(columnIndex);
	}

	public byte[] getBytes(String columnName) throws SQLException {
		return this.set.getBytes(columnName);
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return this.set.getCharacterStream(columnIndex);
	}

	public Clob getClob(int i) throws SQLException {
		return this.set.getClob(i);
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return this.set.getDate(columnIndex, cal);
	}

	public Date getDate(int columnIndex) throws SQLException {
		return this.set.getDate(columnIndex);
	}

	public double getDouble(int columnIndex) throws SQLException {
		return this.set.getDouble(columnIndex);
	}

	public float getFloat(int columnIndex) throws SQLException {
		return this.set.getFloat(columnIndex);
	}

	public int getInt(int columnIndex) throws SQLException {
		return this.set.getInt(columnIndex);
	}

	public long getLong(int columnIndex) throws SQLException {
		return this.set.getLong(columnIndex);
	}

	public Object getObject(int i, Map<String, Class<?>> map)
			throws SQLException {
		return this.set.getObject(i, map);
	}

	public Object getObject(int columnIndex) throws SQLException {
		Object o = this.set.getObject(columnIndex);
		if (o != null) {
			if (Blob.class.isAssignableFrom(o.getClass())) {
				Blob b = (Blob) o;
				InputStream is = b.getBinaryStream();
				try {
					o = RowUtils.processBlob(is);
				} finally {
					InputOutputUtils.closeQuietly(is);
				}
			}
		}

		return o;
	}

	public Ref getRef(int i) throws SQLException {
		return this.set.getRef(i);
	}

	public int getRow() throws SQLException {
		return this.set.getRow();
	}

	public short getShort(int columnIndex) throws SQLException {
		return this.set.getShort(columnIndex);
	}

	public Statement getStatement() throws SQLException {
		return this.set.getStatement();
	}

	public String getString(int columnIndex) throws SQLException {
		return this.set.getString(columnIndex);
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return this.set.getTime(columnIndex, cal);
	}

	public Time getTime(int columnIndex) throws SQLException {
		return this.set.getTime(columnIndex);
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		return this.set.getTimestamp(columnIndex, cal);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return this.set.getTimestamp(columnIndex);
	}

	public int getType() throws SQLException {
		return this.set.getType();
	}

	public boolean isAfterLast() throws SQLException {
		return hasResults() && this.set.isAfterLast();
	}

	public boolean isBeforeFirst() throws SQLException {
		return hasResults() && this.set.isBeforeFirst();
	}

	public boolean isFirst() throws SQLException {
		return hasResults() && this.set.isFirst();
	}

	public boolean isLast() throws SQLException {
		return hasResults() && this.set.isLast();
	}

	public boolean last() throws SQLException {
		return hasResults() && this.set.last();
	}

	public boolean next() throws SQLException {
		if (this.set == null)
			return false;
		boolean hasNext = this.set.next();
		if (!hasNext)
			close();
		return hasResults() && hasNext;
	}

	public boolean previous() throws SQLException {
		return hasResults() && this.set.previous();
	}

	public boolean relative(int rows) throws SQLException {
		return hasResults() && this.set.relative(rows);
	}

	public boolean wasNull() throws SQLException {
		return this.set.wasNull();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.services.sql.ROResultSet#getMetaData()
	 */
	public ResultSetMetaData getMetaData() throws SQLException {
		return this.set.getMetaData();
	}

	public URL getURL(int columnIndex) throws SQLException {
		return this.set.getURL(columnIndex);
	}

	private int[] getTypes() throws SQLException {
		ResultSetMetaData data = getMetaData();
		int c = data.getColumnCount();
		int[] types = new int[c];
		for (int i = 0; i < c; i++) {
			types[i] = data.getColumnType(i + 1);
		}
		return types;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.services.sql.ROResultSet#getObjects()
	 */
	public Object[][] getObjects() throws SQLException {
		List<Object[]> objects = new ArrayList<Object[]>();
		try {
			while (next()) {
				objects.add(RowUtils.parseRow(this, getTypes()));
			}
		} finally {
			close();
		}
		return objects.toArray(new Object[][] {});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.services.sql.ROResultSet#streamResult(java.io.ObjectOutputStream)
	 */
	public void streamResult(ObjectOutputStream stream) throws SQLException,
			IOException {
		try {
			while (next()) {
				stream.writeObject(RowUtils.parseRow(this, getTypes()));
			}
		} finally {
			close();
		}
	}

	private String uri;

	private String query;

	public String getQuery() {
		return this.query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
