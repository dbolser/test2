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

import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.genomebuilder.model.GeneNameType;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GeneNameImpl;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * Converts the table gene name into the object gene name
 *
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class GeneNameMapRowMapper
	extends AbstractCollectionMapRowMapper<Persistable<GeneName>> {

	public void existingObject(Collection<Persistable<GeneName>> currentValue,
		ROResultSet resultSet, int position) throws SQLException {
		String id = resultSet.getString(2);
		GeneNameType type = GeneNameType.getGeneNameTypeByName(resultSet.getString(3));
		GeneName name = new GeneNameImpl(resultSet.getString(4), type);
		currentValue.add(new SimpleWrapperPersistable<GeneName>(name, id));
	}
}
