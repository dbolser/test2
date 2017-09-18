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
 * File: ROResultSet.java
 * Created by: dstaines
 * Created on: Nov 16, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.services.sql;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Subset of ResultSet
 * 
 * @author dstaines
 * 
 */
public interface ROResultSet {
	
	public String getQuery();
	
	public void setQuery(String sql);
	
	public String getUri();
	
	public void setUri(String uri);
	
	public boolean hasResults();

	public Object[][] getObjects() throws SQLException;

	public void streamResult(ObjectOutputStream stream) throws SQLException,
			IOException;

	public void close() throws SQLException;

	public boolean wasNull() throws SQLException;

	public boolean isBeforeFirst() throws SQLException;

	public boolean isAfterLast() throws SQLException;

	public boolean isFirst() throws SQLException;

	public boolean isLast() throws SQLException;

	public void beforeFirst() throws SQLException;

	public void afterLast() throws SQLException;

	public boolean first() throws SQLException;

	public boolean last() throws SQLException;

	public boolean next() throws SQLException;

	public int getRow() throws SQLException;

	public boolean absolute(int row) throws SQLException;

	public boolean relative(int rows) throws SQLException;

	public boolean previous() throws SQLException;

	public abstract ResultSetMetaData getMetaData() throws SQLException;

	public abstract String getString(int columnIndex) throws SQLException;

	public abstract boolean getBoolean(int columnIndex) throws SQLException;

	public abstract byte getByte(int columnIndex) throws SQLException;

	public abstract short getShort(int columnIndex) throws SQLException;

	public abstract int getInt(int columnIndex) throws SQLException;

	public abstract long getLong(int columnIndex) throws SQLException;

	public abstract float getFloat(int columnIndex) throws SQLException;

	public abstract double getDouble(int columnIndex) throws SQLException;

	public abstract byte[] getBytes(int columnIndex) throws SQLException;

	public abstract java.sql.Date getDate(int columnIndex) throws SQLException;

	public abstract java.sql.Time getTime(int columnIndex) throws SQLException;

	public abstract java.sql.Timestamp getTimestamp(int columnIndex)
			throws SQLException;

	public abstract java.io.InputStream getAsciiStream(int columnIndex)
			throws SQLException;

	public abstract java.io.InputStream getBinaryStream(int columnIndex)
			throws SQLException;

	public abstract Object getObject(int columnIndex) throws SQLException;

	public abstract java.io.Reader getCharacterStream(int columnIndex)
			throws SQLException;

	public abstract BigDecimal getBigDecimal(int columnIndex)
			throws SQLException;

	public abstract java.net.URL getURL(int columnIndex) throws SQLException;

	public boolean isClosed();
	
}
