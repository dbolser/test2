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

package uk.ac.ebi.proteome.materializer.misc;

import uk.ac.ebi.proteome.util.sql.defaultmappers.AbstractMapRowMapper;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

import java.sql.SQLException;

/**
 * Defaults the population of the key field to an Object even though the
 * underlying key type is a String
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractStringMapRowMapper<T> extends AbstractMapRowMapper<Object, T> {

	public Object getKey(ROResultSet resultSet) throws SQLException {
		return resultSet.getString(1);
	}
}
