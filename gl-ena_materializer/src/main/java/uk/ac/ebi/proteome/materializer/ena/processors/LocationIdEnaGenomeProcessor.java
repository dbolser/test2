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

package uk.ac.ebi.proteome.materializer.ena.processors;

import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.materializer.ena.executor.SimpleExecutor;
import uk.ac.ebi.proteome.services.ServiceContext;

public class LocationIdEnaGenomeProcessor extends EnaGenomeProcessor {

	public LocationIdEnaGenomeProcessor(EnaGenomeConfig config,
			ServiceContext context) {
		this(config, context, new XmlDatabaseReferenceTypeRegistry());
	}

	public LocationIdEnaGenomeProcessor(EnaGenomeConfig config,
			ServiceContext context, DatabaseReferenceTypeRegistry registry) {
		super(config, context, registry, new SimpleExecutor());
		this.addProcessor(new LocationIdProcessor(registry));
	}
	
}
