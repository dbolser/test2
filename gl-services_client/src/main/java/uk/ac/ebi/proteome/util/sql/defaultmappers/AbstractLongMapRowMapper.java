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
 * Provides a similar function as {@link AbstractStringMapRowMapper} but
 * returns the first column as the key value but as a Long. This same method
 * can be used for int/Integer based result sets
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractLongMapRowMapper<T> extends AbstractMapRowMapper<Long, T> {

	/**
	 * Returns the first column as the keyed value & is a Long
	 */
	public Long getKey(ROResultSet resultSet) throws SQLException {
		return resultSet.getLong(1);
	}

}
