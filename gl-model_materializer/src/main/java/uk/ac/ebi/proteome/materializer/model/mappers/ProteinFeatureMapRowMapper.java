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

import uk.ac.ebi.proteome.genomebuilder.model.ProteinFeature;
import uk.ac.ebi.proteome.genomebuilder.model.ProteinFeatureType;
import uk.ac.ebi.proteome.genomebuilder.model.ProteinFeatureSource;
import uk.ac.ebi.proteome.genomebuilder.model.impl.ProteinFeatureImpl;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * Used for mapping a result set into a protein feature object
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ProteinFeatureMapRowMapper
	extends AbstractCollectionMapRowMapper<Persistable<ProteinFeature>> {

	public void existingObject(
		Collection<Persistable<ProteinFeature>> currentValue,
		ROResultSet resultSet, int position) throws SQLException {

		Long id = resultSet.getLong(2);
		ProteinFeatureType type = ProteinFeatureType.forInt(resultSet.getInt(3));
		String description = resultSet.getString(4);
        ProteinFeatureSource pSource = null;
        if (!StringUtils.isEmpty(resultSet.getString(5))) {
            char source = resultSet.getString(5).charAt(0);
            pSource = ProteinFeatureSource.valueOf(source);
        }
        int startAt = resultSet.getInt(6);
		int endAt = resultSet.getInt(7);
		ProteinFeatureImpl f =
                new ProteinFeatureImpl(type, description, description, startAt, endAt, pSource);

		currentValue.add(new SimpleWrapperPersistable<ProteinFeature>(f, id));
	}
}
