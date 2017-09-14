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

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DatabaseReferenceImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * Used to retrieve database references from the persisted model tables & will
 * map into a Map<Object,List<Persistable<DatabaseReference>>> (where the map
 * key is actually a Long).
 *
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class XrefMapRowMapper extends
		AbstractCollectionMapRowMapper<Persistable<DatabaseReference>> {

	private final DatabaseReferenceTypeRegistry xrefRegistry;

	public XrefMapRowMapper(Registry registry) {
		this.xrefRegistry = registry.get(DatabaseReferenceTypeRegistry.class);
	}

	protected DatabaseReferenceTypeRegistry getXrefRegistry() {
		return xrefRegistry;
	}

	public void existingObject(
			Collection<Persistable<DatabaseReference>> currentValue,
			ROResultSet resultSet, int position) throws SQLException {

		Long id = resultSet.getLong(2);
		DatabaseReferenceImpl ref = new DatabaseReferenceImpl();

		DatabaseReferenceType type = getXrefRegistry().getType(
				resultSet.getInt(3));
		ref.setDatabaseReferenceType(type);
		ref.setPrimaryIdentifier(resultSet.getString(4));
		ref.setSecondaryIdentifier(resultSet.getString(5));
		ref.setTertiaryIdentifier(resultSet.getString(6));
		ref.setQuarternaryIdentifier(resultSet.getString(7));
		ref.setDescription(resultSet.getString(8));
		ref.setSourceDb(getXrefRegistry().getType(resultSet.getInt(9)));
		ref.setSourceId(resultSet.getString(10));
		if (resultSet.getObject(11) != null)
			ref.setIdentity(resultSet.getDouble(11));

		currentValue.add(new SimpleWrapperPersistable<DatabaseReference>(ref,
				id));
	}
}
