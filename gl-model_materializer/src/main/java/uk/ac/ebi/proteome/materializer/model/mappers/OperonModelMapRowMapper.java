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

import uk.ac.ebi.proteome.genomebuilder.model.Operon;
import uk.ac.ebi.proteome.genomebuilder.model.impl.OperonImpl;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Brings back an instance of the Operon model
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class OperonModelMapRowMapper extends AbstractCollectionMapRowMapper<Persistable<Operon>> {
	public void existingObject(Collection<Persistable<Operon>> currentValue,
		ROResultSet resultSet, int position) throws SQLException {

		Long id = resultSet.getLong(2);
		OperonImpl o = new OperonImpl();
		o.setName(resultSet.getString(3));
		currentValue.add(new SimpleWrapperPersistable<Operon>(o, id));
	}
}
