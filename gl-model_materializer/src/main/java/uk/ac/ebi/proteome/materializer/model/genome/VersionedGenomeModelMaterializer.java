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
 * File: VersionedComponentAcGenomeModelMaterializer.java
 * Created by: dstaines
 * Created on: Feb 3, 2010
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer.model.genome;

import java.util.Date;

import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.materializer.version.VersionUtils;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.services.version.VersionService;

/**
 * @author dstaines
 * 
 */
public class VersionedGenomeModelMaterializer extends
		GenomeModelMaterializer {

	private final VersionService srv;

	public VersionedGenomeModelMaterializer(Registry registry,
			VersionService srv) {
		super(registry);
		this.srv = srv;
	}

	@Override
	public MaterializedDataInstance<Persistable<Genome>, String> getMaterializedDataInstance(
			Object... args) {
		MaterializedDataInstance<Persistable<Genome>, String> materializedDataInstance = super
				.getMaterializedDataInstance(args);
		Genome genome = materializedDataInstance.getData()
				.getPersistableObject();
		VersionUtils.versionGenome(srv, genome);
		Date dateForGenome = VersionUtils.getDateForGenome(srv, genome);
		genome.setCreationDate(dateForGenome);
		genome.setUpdateDate(dateForGenome);
		genome.setVersion(String.valueOf(VersionUtils
				.getVersionForGenome(genome)));
		return materializedDataInstance;
	}
}
