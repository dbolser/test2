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

import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene.PseudogeneType;
import uk.ac.ebi.proteome.genomebuilder.model.impl.AnnotatedGeneImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.PseudogeneImpl;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class PseudogeneModelMapRowMapper extends
		AbstractCollectionMapRowMapper<Persistable<Pseudogene>> {

	public void existingObject(Collection<Persistable<Pseudogene>> currentValue,
		ROResultSet resultSet, int position) throws SQLException {
		Long id = resultSet.getLong(2);
		PseudogeneImpl pseudogene = new PseudogeneImpl(new AnnotatedGeneImpl());
		pseudogene.setName(resultSet.getString(4));
		pseudogene.setIdentifyingId(resultSet.getString(3));
		pseudogene.setType(PseudogeneType.valueOf(resultSet.getInt(5)));
		currentValue.add(new SimpleWrapperPersistable<Pseudogene>(pseudogene,id));
	}
}
