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
import java.util.List;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.genomebuilder.model.GeneNameType;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DatabaseReferenceImpl;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * @author $Author$
 * @version $Revision$
 */
public class PombeEmblRnageneWithXrefModelMapRowMapper extends
		EmblRnageneWithXrefModelMapRowMapper {

	public PombeEmblRnageneWithXrefModelMapRowMapper(Registry registry) {
		super(registry);
	}

	protected Rnagene makeRnagene(ROResultSet resultSet) throws SQLException {
		Rnagene rnagene = super.makeRnagene(resultSet);
		List<GeneName> names = rnagene.getNameMap().get(
				GeneNameType.ORDEREDLOCUSNAMES);
		if (names != null && names.size() != 0) {
			DatabaseReferenceImpl reference = new DatabaseReferenceImpl();
			DatabaseReferenceType type = getXrefRegistry().getTypeForName(
					"GeneDB_Spombe");
			reference.setDatabaseReferenceType(type);
			reference.setPrimaryIdentifier(names.get(0).getName());
			rnagene.addDatabaseReference(reference);
			rnagene.setDescription(rnagene.getDescription().replaceAll(
					"\\[Source:.*\\]",
					"[Source:" + type.getEnsemblName() + ";Acc:"
							+ reference.getPrimaryIdentifier() + "]"));
		}
		return rnagene;
	}
}
