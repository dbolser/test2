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

import java.sql.SQLException;
import java.util.Collection;

import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichLocation;

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;

/**
 * Used to map a result set into an entity location with optional override for
 * working with ranked locations. Also deals with circular locations for
 * elements which span the ori.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class LocationMapRowMapper extends
		AbstractCollectionMapRowMapper<Persistable<EntityLocation>> {

	public void existingObject(
			Collection<Persistable<EntityLocation>> currentValue,
			ROResultSet resultSet, int position) throws SQLException {

		Long id = resultSet.getLong(2);
		int min = resultSet.getInt(3);
		int max = resultSet.getInt(4);
		int strandValue = resultSet.getInt(5);
		int state = resultSet.getInt(6);
		boolean circular = resultSet.getBoolean(8);
		int length = resultSet.getInt(9);

		boolean min_fuzzy = resultSet.getBoolean(10);
		boolean max_fuzzy = resultSet.getBoolean(11);

		EntityLocation.MappingState mappingState = EntityLocation.MappingState
				.valueOf(state);

		RichLocation richLocation = new SimpleRichLocation(new SimplePosition(
				min_fuzzy, false, min), new SimplePosition(false, max_fuzzy,
				max), getRank(resultSet), RichLocation.Strand
				.forValue(strandValue));

		if (circular) {
			richLocation.setCircularLength(length);
			richLocation = LocationUtils.splitCircularLocation(richLocation);
		}

		EntityLocation location = new DelegatingEntityLocation(richLocation,
				mappingState);

		currentValue.add(new SimpleWrapperPersistable<EntityLocation>(location,
				id));
	}

	/**
	 * Can be overriden to provide custom code for retriving ranks
	 *
	 * @throws SQLException
	 *             Never thrown but here for other code which uses the result
	 *             set
	 */
	public int getRank(ROResultSet resultSet) throws SQLException {
		return resultSet.getInt(7);
	}
}
