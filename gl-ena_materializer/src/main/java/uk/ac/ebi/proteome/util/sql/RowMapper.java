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

import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * Generic class which provides a convenient way of defining a callback/closure
 * to generate an object from a given {@link ROResultSet} object. This
 * is taken from the Spring JdbcTemplate code & associated hierarchy and should
 * be used in conjunction with {@link SqlServiceTemplateImpl}
 *
 * <p>
 * The user is allowed to work with this object's lifecycle in whatever manner
 * it wishes to however it is recommended that a new object is created per
 * mapper run. This allows you to perform complex mappings of result sets
 * to Objects if required.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public interface RowMapper<T> {

	/**
	 * Main method for mappers which attempts to convert a given row from a
	 * results set into an Object.
	 *
	 * @param resultSet The result set which will be on the given position
	 * @param position The current position in the iteration
	 * @return The given parametertised object
	 * @throws SQLException Thrown to ensure that you the programmer do not handle
	 * any SQLExceptions as that is the responsibilty of the calling class
	 */
	T mapRow(ROResultSet resultSet, int position) throws SQLException;

}
