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
 * File: ObjectArrayResultMetaData.java
 * Created by: dstaines
 * Created on: Nov 20, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.services.sql.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

public class ObjectArrayResultSetMetaData implements ResultSetMetaData, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int[] types;

	public ObjectArrayResultSetMetaData() {
		types = new int[0];
	}

	public ObjectArrayResultSetMetaData(int[] types) {
		this.types = types;
	}

	public ObjectArrayResultSetMetaData(Object[] row) {
		setTypes(row);
	}

	public int[] getTypes() {
		return this.types;
	}

	public void setTypes(int types[]) {
		this.types = types;
	}

	public void setTypes(Object row[]) {
		types = new int[row.length];
		for (int i = 0; i < row.length; i++)
			types[i] = getType(row[i]);

	}

	public static int getType(Object o) {
		if (o == null) {
			return Types.NULL;
		} else if (o instanceof Date) {
			return Types.DATE;
		} else if (o instanceof String) {
			return Types.VARCHAR;
		} else if (o instanceof Boolean) {
			return Types.BOOLEAN;
		} else if (o instanceof Integer) {
			return Types.INTEGER;
		} else if (o instanceof Long) {
			return Types.BIGINT;
		} else if (o instanceof Short) {
			return Types.SMALLINT;
		} else if (o instanceof BigDecimal) {
			return Types.NUMERIC;
		} else if (o instanceof Float) {
			return Types.FLOAT;
		} else if (o instanceof Double) {
			return Types.DOUBLE;
		} else {
			return Types.BLOB;
		}
	}

	public void setColumnType(int i, Object obj) {
		types[i - 1] = getType(obj);
	}

	public void setColumnType(int i, int type) {
		types[i - 1] = type;
	}

	public String getCatalogName(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public String getColumnClassName(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public int getColumnCount() throws SQLException {
		return types.length;
	}

	public int getColumnDisplaySize(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public String getColumnLabel(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public String getColumnName(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public int getColumnType(int column) throws SQLException {
		return types[column-1];
	}

	public String getColumnTypeName(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public int getPrecision(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public int getScale(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public String getSchemaName(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public String getTableName(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isAutoIncrement(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isCaseSensitive(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isCurrency(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isDefinitelyWritable(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public int isNullable(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isReadOnly(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isSearchable(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isSigned(int column) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isWritable(int column) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}

}
