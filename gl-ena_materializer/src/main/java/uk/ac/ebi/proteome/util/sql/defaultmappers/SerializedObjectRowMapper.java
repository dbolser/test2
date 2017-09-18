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

package uk.ac.ebi.proteome.util.sql.defaultmappers;

import java.io.Serializable;
import java.sql.SQLException;

import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.util.SerializableObjectHolder;
import uk.ac.ebi.proteome.util.UtilUncheckedException;
import uk.ac.ebi.proteome.util.sql.RowMapper;

/**
 * This mapper attempts to perform conversion of the raw byte representation of
 * an Object (as held in blob form in Oracle) into an Object. This object is
 * then checked to see if it is assignable to a {@link SerializableObjectHolder}
 * (the main way of indicating to the SqlService that we expect this Object to
 * be stored as a blob). If so then the object is retrived from
 * {@link SerializableObjectHolder#get()}, otherwise it will return the object
 * serialized from the raw bytes.
 * 
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 * @param <T>
 *            The type of object which is expected out from this RowMapper. Must
 *            implement {@link Serializable}
 */
public class SerializedObjectRowMapper<T extends Serializable> implements
		RowMapper<T> {

	/**
	 * Performs mapping of the bytes in column 1 to the specified object of type
	 * T
	 */
	public T mapRow(ROResultSet resultSet, int position) throws SQLException {
		return mapColumn(resultSet, 1);
	}

	/**
	 * For a given column this method will convert from the byte representation
	 * of the column to the given object
	 */
	@SuppressWarnings("unchecked")
	public T mapColumn(ROResultSet resultSet, int column) throws SQLException {
		Object resultSetObject = resultSet.getObject(column);
		// Object resultSetObject =
		// RowUtils.processBlob(resultSet.getBytes(column));
		Object output = null;
		if (resultSetObject == null) {
			output = null;
		} else if (SerializableObjectHolder.class
				.isAssignableFrom(resultSetObject.getClass())) {
			try {
				output = ((SerializableObjectHolder) resultSetObject).get();
			} catch (UtilUncheckedException e) {
				if (e.getCause() != null
						&& ClassNotFoundException.class.isAssignableFrom(e
								.getCause().getClass())) {
					output = resultSetObject;
				} else {
					throw e;
				}
			}
		} else {
			output = resultSetObject;
		}

		return (T) output;
	}
}
