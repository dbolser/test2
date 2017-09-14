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

/**
 * File: GenomeRowMapper.java
 * Created by: dstaines
 * Created on: Oct 5, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer.model.mappers;

import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo.OrganismNameType;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomeImpl;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.resolver.Scope;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.util.sql.RowMapper;

public class GenomeRowMapper implements RowMapper<Persistable<Genome>> {

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.util.sql.RowMapper#mapRow(uk.ac.ebi.proteome.services.sql.ROResultSet,
	 *      int)
	 */
	public Persistable<Genome> mapRow(ROResultSet resultSet, int position)
			throws SQLException {
		GenomeImpl genome = new GenomeImpl(String.valueOf(resultSet.getInt(1)), resultSet
				.getInt(2), resultSet.getString(3), resultSet.getString(4),
				Scope.getByNumber(resultSet.getInt(5)));
		genome.setOrganismName(OrganismNameType.FULL, resultSet.getString(6));
		genome.setOrganismName(OrganismNameType.SHORT, resultSet.getString(7));
		genome.setOrganismName(OrganismNameType.FILE, resultSet.getString(8));
		genome.setOrganismName(OrganismNameType.SQL, resultSet.getString(9));
		genome.setOrganismName(OrganismNameType.GR, resultSet.getString(10));
		String linStr = resultSet.getString(11);
        if (!StringUtils.isEmpty(linStr))
			genome.getLineage().addAll(Arrays.asList(linStr.split(";")));
        return new SimpleWrapperPersistable<Genome>(genome, genome.getId());
	}

}
