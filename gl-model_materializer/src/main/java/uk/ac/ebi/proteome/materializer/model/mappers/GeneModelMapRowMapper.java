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

import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GeneImpl;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

import java.sql.SQLException;
import java.util.Collection;

/**
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class GeneModelMapRowMapper extends AbstractCollectionMapRowMapper<Persistable<Gene>> {
	public void existingObject(Collection<Persistable<Gene>> currentValue,
		ROResultSet resultSet, int position) throws SQLException {
		
		Long id = resultSet.getLong(2);
		GeneImpl gene = new GeneImpl();
		gene.setIdentifyingId(resultSet.getString(3));
		gene.setName(resultSet.getString(4));
        gene.setUniprotKbAc(resultSet.getString(5));
        gene.setDescription(resultSet.getString(6));
        gene.setPublicId(resultSet.getLong(7));
        currentValue.add(new SimpleWrapperPersistable<Gene>(gene,id));
	}
}
