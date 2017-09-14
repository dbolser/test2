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

import java.sql.SQLException;

import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * Implementation of map row mapper which expects the first column is the
 * keyed value & is a String.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 * @param <T> The type of object which this map stores as its value
 */
public abstract class AbstractStringMapRowMapper<T> extends AbstractMapRowMapper<String, T> {

	/**
	 * Returns the first column of the result set as a String & is the
	 * result key
	 */
	public String getKey(ROResultSet resultSet) throws SQLException {
		return resultSet.getString(1);
	}
}
