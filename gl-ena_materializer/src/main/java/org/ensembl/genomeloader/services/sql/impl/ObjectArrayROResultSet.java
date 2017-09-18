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
 * File: ObjectArrayROResultSet.java
 * Created by: dstaines
 * Created on: Nov 20, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.services.sql.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import org.ensembl.genomeloader.services.sql.ROResultSet;

/**
 * @author dstaines
 *
 */
public class ObjectArrayROResultSet implements ROResultSet {

	protected String uri;

	protected String query;

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

	protected boolean closed = false;

	public boolean isClosed() {
		return this.closed;
	}

	public boolean hasResults() {
		return objs != null;
	}

	public ObjectArrayROResultSet() {
		objs = new Object[][] {};
	}

	ObjectArrayResultSetMetaData data = new ObjectArrayResultSetMetaData();

	protected int n = -1;

	protected void setRow(int n) {
		this.n = n;
		this.row = objs[n];
		data.setTypes(objs[n]);
	}

	private Object[][] objs;

	public ObjectArrayROResultSet(ROResultSet set) throws SQLException {
		this.objs = set.getObjects();
		this.query = set.getQuery();
		this.uri = set.getUri();
		if (objs.length > 0) {
			data = new ObjectArrayResultSetMetaData(objs[0]);
		}
	}

	public ObjectArrayROResultSet(Object[][] objs, String uri, String query) {
		this.objs = objs;
		this.query = query;
		this.uri = uri;
		if (objs.length > 0) {
			data = new ObjectArrayResultSetMetaData(objs[0]);
		}
	}

	public Object[][] getObjects() throws SQLException {
		return this.objs;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#absolute(int)
	 */
	public boolean absolute(int row) throws SQLException {
		if (row < objs.length && row > 0) {
			setRow(n - 1);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#afterLast()
	 */
	public void afterLast() throws SQLException {
		setRow(objs.length);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#beforeFirst()
	 */
	public void beforeFirst() throws SQLException {
		setRow(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#close()
	 */
	public void close() throws SQLException {
		closed = true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#first()
	 */
	public boolean first() throws SQLException {
		return n == 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getAsciiStream(int)
	 */
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		return (BigDecimal) obj;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getBinaryStream(int)
	 */
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getBoolean(int)
	 */
	public boolean getBoolean(int columnIndex) throws SQLException {

		Object columnObject = getObj(columnIndex);

		// In SQL, boolean is commonly mapped into a bit datatype, 0 or 1
		// but it could be also mapped into a varchar (ie a String) "0" or "1"
		// so we have to be able to deal with all these cases and return a
		// boolean value true or false
		// whatever datatype we have as an input
		if (columnObject == null) {
			return false;
		}
		else if (Boolean.class.isAssignableFrom(columnObject.getClass())) {
			return (Boolean) columnObject;
		}
		else if (String.class.isAssignableFrom(columnObject.getClass())) {
			if ("1".equals(columnObject)) {
				return true;
			}
			else if ("0".equals(columnObject)) {
				return false;
			}
			else {
				throw new SQLException("Invalid boolean string value "
						+ columnObject + " object in getBoolean");
			}
		}
		else if (Number.class.isAssignableFrom(columnObject.getClass())) {
			int value = ((Number)columnObject).intValue();
			if (value == 1) {
				return true;
			}
			else if (value == 0) {
				return false;
			}
			else {
				throw new SQLException("Invalid boolean numeric value "
						+ columnObject + " object in getBoolean");
			}
		}
		else {
			throw new SQLException("Don't know how to process "
					+ columnObject.getClass().getName()
					+ " object in getBoolean");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getByte(int)
	 */
	public byte getByte(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getBytes(int)
	 */
	public byte[] getBytes(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getCharacterStream(int)
	 */
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		if (obj == null) {
			return null;
		}
		return new StringReader(String.valueOf(obj));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getDate(int)
	 */
	public Date getDate(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		if (obj == null) {
			return null;
		} else if (obj instanceof String) {
			return Date.valueOf((String) obj);
		} else if (obj instanceof Integer) {
			return new Date((Integer) obj);
		} else if (obj instanceof Long) {
			return new Date((Long) obj);
		} else if (obj instanceof java.util.Date) {
			return new Date(((java.util.Date) obj).getTime());
		} else if (obj instanceof Date) {
			return (Date) obj;
		} else {
			throw new SQLException("Cannot convert value of class "
					+ obj.getClass().getName() + " into "+Date.class.getName());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getDouble(int)
	 */
	public double getDouble(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		if (obj == null) {
			return 0;
		} else if (obj instanceof String) {
			return Double.valueOf((String) obj);
		} else if (obj instanceof BigDecimal) {
			return ((BigDecimal) obj).doubleValue();
		} else {
			throw new SQLException("Cannot convert value of class "
					+ obj.getClass().getName() + " into a double");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getFloat(int)
	 */
	public float getFloat(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		if (obj == null) {
			return 0;
		} else if (obj instanceof String) {
			return Float.valueOf((String) obj);
		} else if (obj instanceof BigDecimal) {
			return ((BigDecimal) obj).floatValue();
		} else {
			throw new SQLException("Cannot convert value of class "
					+ obj.getClass().getName() + " into a float");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getInt(int)
	 */
	public int getInt(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		if (obj == null) {
			return 0;
		} else if (obj instanceof Integer) {
			return ((Integer) obj).intValue();
		} else if (obj instanceof String) {
			return Integer.valueOf((String) obj);
		} else if (obj instanceof BigDecimal) {
			return ((BigDecimal) obj).intValue();
		} else {
			throw new SQLException("Cannot convert value of class "
					+ obj.getClass().getName() + " into an integer");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getLong(int)
	 */
	public long getLong(int columnIndex) throws SQLException {
		if (getObj(columnIndex) == null) {
			return 0;
		} else {
			return ((BigDecimal) getObj(columnIndex)).longValue();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getMetaData()
	 */
	public ResultSetMetaData getMetaData() throws SQLException {
		return data;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getObject(int)
	 */
	public Object getObject(int columnIndex) throws SQLException {
		return getObj(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getRow()
	 */
	public int getRow() throws SQLException {
		return n;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getShort(int)
	 */
	public short getShort(int columnIndex) throws SQLException {
		return (Short) getObj(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getString(int)
	 */
	public String getString(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		if (obj == null) {
			return null;
		} else {
			return String.valueOf(getObj(columnIndex));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getTime(int)
	 */
	public Time getTime(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		if (obj == null) {
			return null;
		} else {
			return new Time(((Date) getObj(columnIndex)).getTime());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		if (obj == null) {
			return null;
		} else {
			if (obj instanceof java.util.Date) {
				return new Timestamp(((java.util.Date) obj).getTime());
			} else if (obj instanceof Date) {
				return new Timestamp(((Date) getObj(columnIndex)).getTime());
			} else {
				throw new SQLException("Cannot convert value of class "
						+ obj.getClass().getName() + " into "+Timestamp.class.getName());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#getURL(int)
	 */
	public URL getURL(int columnIndex) throws SQLException {
		Object obj = getObj(columnIndex);
		if (obj == null) {
			return null;
		}
		String str = null;
		try {
			str = String.valueOf(obj);
			return new URL(str);
		} catch (MalformedURLException e) {
			throw new SQLException("Retrieved string " + str
					+ " is not a valid URL");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#isAfterLast()
	 */
	public boolean isAfterLast() throws SQLException {
		return n >= objs.length;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#isBeforeFirst()
	 */
	public boolean isBeforeFirst() throws SQLException {
		return n <= 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#isFirst()
	 */
	public boolean isFirst() throws SQLException {
		return n == 1;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#isLast()
	 */
	public boolean isLast() throws SQLException {
		return n == objs.length;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#last()
	 */
	public boolean last() throws SQLException {
		setRow(objs.length);
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#next()
	 */
	public boolean next() throws SQLException {
		if (n + 1 < objs.length) {
			setRow(n + 1);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#previous()
	 */
	public boolean previous() throws SQLException {
		if (n > 0) {
			setRow(n - 1);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#relative(int)
	 */
	public boolean relative(int rows) throws SQLException {
		if ((n + rows) <= objs.length && (n + rows) > 0) {
			setRow(n + rows);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#wasNull()
	 */
	public boolean wasNull() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	protected Object[] row;

	protected Object getObj(int i) throws SQLException {
		if (i > row.length) {
			throw new SQLException("Column " + i + " of row " + n
					+ " not found");
		}
		return row[i - 1];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.services.sql.ROResultSet#streamResult(java.io.ObjectOutputStream)
	 */
	public void streamResult(ObjectOutputStream stream) throws SQLException,
			IOException {
		stream.writeObject(new ObjectArrayResultSetMetaData(data.getTypes()));
		while (this.next()) {
			stream.writeObject(row);
		}
	}

}
