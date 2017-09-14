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

import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.genomebuilder.model.impl.TranscriptImpl;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

import java.util.Collection;
import java.sql.SQLException;

/**
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class TranscriptModelMapRowMapper extends AbstractCollectionMapRowMapper<Persistable<Transcript>> {
	public void existingObject(Collection<Persistable<Transcript>> currentValue, ROResultSet resultSet, int position) throws SQLException {
		Long id = resultSet.getLong(2);
		TranscriptImpl trans = new TranscriptImpl();
		trans.setName(resultSet.getString(3));
		trans.setPromoter(resultSet.getString(4));
		trans.setCoTranscribedUnit(resultSet.getString(5));
		currentValue.add(new SimpleWrapperPersistable<Transcript>(trans, id));
	}
}
