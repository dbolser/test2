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

import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.resolver.DataItem;
import uk.ac.ebi.proteome.resolver.SourceDefinition;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.services.version.Section;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Maps the results of a row representing a data item into the equivalent
 * object structure.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class DataItemMapRowMapper extends AbstractCollectionMapRowMapper<DataItem> {

	public void existingObject(Collection<DataItem> currentValue, ROResultSet resultSet, int position) throws SQLException {
		currentValue.add(mapItem(resultSet));
	}

	protected DataItem mapItem(ROResultSet resultSet) throws SQLException {
		DataItem item = new DataItem();
		item.setIdentifier(resultSet.getString(2));
		item.setSource(mapDef(resultSet));
		item.setSection(mapSection(resultSet));
		return item;
	}

	protected SourceDefinition mapDef(ROResultSet resultSet) throws SQLException {
		SourceDefinition def = new SourceDefinition();
		def.setName(resultSet.getString(3));
		def.setType(resultSet.getString(4));
		return def;
	}

	protected Section mapSection(ROResultSet resultSet) throws SQLException {
		Section section = new Section();
		section.setDatasource(resultSet.getString(3));
		
		//If section is null then name is the ds name
		String sectionName = resultSet.getString(2);
		section.setSection( (sectionName == null) ? section.getDatasource() : sectionName );
		
		section.setId(resultSet.getInt(5));
		section.setSectionArchiveId(resultSet.getInt(6));
		section.setDataChecksum(resultSet.getString(7));
		return section;
	}
}
