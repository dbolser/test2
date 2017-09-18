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
 * File: RowUtils.java
 * Created by: dstaines
 * Created on: Nov 20, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.util.sql;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;

import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * @author dstaines
 * 
 */
public class RowUtils {

	public static Object[] parseRow(ROResultSet rs, int[] types)
			throws SQLException {
		Object[] objs = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			switch (types[i]) {
			case Types.BINARY:
			case Types.ARRAY:
			case Types.BLOB:
				objs[i] = RowUtils.processBlob(rs.getBytes(i + 1));
				break;
			case Types.TIMESTAMP:
				objs[i] = RowUtils.processTimestamp(rs.getTimestamp(i + 1));
				break;
			case Types.DATE:
				objs[i] = RowUtils.processDate(rs.getDate(i + 1));
				break;
			case Types.CLOB:
				objs[i] = RowUtils.processClob(rs.getCharacterStream(i + 1));
				break;
			case Types.BOOLEAN:
				objs[i] = rs.getBoolean(i + 1);
				break;
			case Types.INTEGER:
				objs[i] = rs.getInt(i + 1);
				break;
			case Types.BIGINT:
				objs[i] = rs.getLong(i + 1);
				break;
			case Types.VARCHAR:
				objs[i] = rs.getString(i + 1);
				break;
			case Types.SMALLINT:
			case Types.BIT:
				objs[i] = rs.getShort(i + 1);
				break;
			case Types.CHAR:
				objs[i] = rs.getString(i + 1);
				break;
			case Types.NULL:
				objs[i] = null;
				break;
			case Types.NUMERIC:
			case Types.DECIMAL:
				objs[i] = rs.getBigDecimal(i + 1);
				break;
			case Types.DOUBLE:
			case Types.FLOAT:
				objs[i] = rs.getDouble(i + 1);
				break;
			default:
				objs[i] = rs.getObject(i + 1);
				break;
			}
		}
		return objs;
	}

	public static java.util.Date processDate(java.sql.Date date) {
		java.util.Date output = null;
		if (null != date) {
			output = new java.util.Date(date.getTime());
		}
		return output;
	}

	public static java.util.Date processTimestamp(java.sql.Timestamp timestamp) {
		java.util.Date outputDate = null;
		if (timestamp != null) {
			outputDate = new java.util.Date(timestamp.getTime());
		}
		return outputDate;
	}

	public static Object processBlob(InputStream stream) throws SQLException {
		Object output = null;
		if (stream != null) {
			try {
				output = new ObjectInputStream(stream).readObject();
			} catch (IOException e) {
				String msg = "Detected IOException whilst reading blob";
				throw new SQLException(msg + " " + e.getMessage());
			} catch (ClassNotFoundException e) {
				String msg = "Output object for blob could not be read";
				throw new SQLException(msg + " " + e.getMessage());
			}
		}
		return output;
	}

	public static Object processBlob(byte[] input) throws SQLException {
		Object output = null;
		if (input != null)
			output = processBlob(new ByteArrayInputStream(input));
		return output;
	}

	public static Object processClob(Clob clob) throws SQLException {
		Object output = null;
		if (clob != null) {
			output = processClob(clob.getCharacterStream());
		}
		return output;
	}

	public static String processClob(Reader reader) throws SQLException {
		StringBuilder buf = new StringBuilder();
		if (reader != null) {
			BufferedReader b = new BufferedReader(reader);
			int c;
			try {
				while ((c = b.read()) != -1) {
					buf.append((char) c);
				}
			} catch (IOException e) {
				String msg = "Could not read CLOB data";
				throw new SQLException(msg + ": " + e.getMessage());
			}
		}
		return buf.toString();
	}

}
