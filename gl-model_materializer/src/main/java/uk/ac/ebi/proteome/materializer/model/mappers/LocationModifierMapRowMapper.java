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

package uk.ac.ebi.proteome.materializer.model.mappers;

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationException;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationInsertion;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationModifier;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Brings back any type of known modifier (currently insertion and exception).
 * Whilst the object returns the generic location modifier object type
 * the implementation of the objects will be either the exception or
 * insertion type & therefore will give you access to the full objects if
 * casted.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class LocationModifierMapRowMapper
	extends AbstractCollectionMapRowMapper<Persistable<EntityLocationModifier>> {

	public void existingObject(
		Collection<Persistable<EntityLocationModifier>> currentValue,
		ROResultSet resultSet, int position) throws SQLException {

		Long id = resultSet.getLong(2);
		String type = resultSet.getString(3);
		int startAt = resultSet.getInt(4);
		int stopAt = resultSet.getInt(5);
		String seq = resultSet.getString(6);
		String aa = resultSet.getString(7);

		EntityLocationModifier modification;

		if(EntityLocationModifier.EXCEPTION_TYPE.equals(type)) {
			modification = new EntityLocationException(startAt, stopAt, seq, aa);
		}
		else if(EntityLocationModifier.INSERTION_TYPE.equals(type)) {
			modification = new EntityLocationInsertion(startAt, stopAt, seq);
		}
		else {
			throw new SQLException("Input type "+type+"for location modification "+
				id+" was of an unknown type");
		}

		currentValue.add(
			new SimpleWrapperPersistable<EntityLocationModifier>(modification, id));
	}
}
