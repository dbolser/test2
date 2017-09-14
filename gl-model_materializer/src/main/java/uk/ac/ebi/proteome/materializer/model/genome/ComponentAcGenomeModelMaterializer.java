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
 * File: GenomeModelMaterializer.java
 * Created by: dstaines
 * Created on: Oct 5, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer.model.genome;

import java.util.Map;

import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.materializer.model.mappers.GenomeRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.AbstractDbDataMaterializer;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.sql.RowMapper;

/**
 * Materializes a genome by the AC of a component that has been fed to it
 * @author dstaines
 *
 */
@Deprecated
public class ComponentAcGenomeModelMaterializer extends
		AbstractDbDataMaterializer<Persistable<Genome>, String> {

	/**
	 * @param registry
	 */
	public ComponentAcGenomeModelMaterializer(Registry registry) {
		super(registry);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.persistence.materializer.DataMaterializer#getMaterializedDataInstance(java.lang.Object[])
	 */
	public MaterializedDataInstance<Persistable<Genome>, String> getMaterializedDataInstance(
			Object... args) {
		String ac = (String) args[0];
		Map<Object, Persistable<Genome>> map = CollectionUtils.createHashMap();
		Persistable<Genome> per = retrieveGenome(ac);
		map.put(ac, per);
		return new MaterializedDataInstance<Persistable<Genome>, String>(map);
	}

	public Persistable<Genome> retrieveGenome(String ac) {
		RowMapper<Persistable<Genome>> mapper = new GenomeRowMapper();
		return getTemplate().queryForObject(getSqlStatement(), mapper, ac);
	}

}
