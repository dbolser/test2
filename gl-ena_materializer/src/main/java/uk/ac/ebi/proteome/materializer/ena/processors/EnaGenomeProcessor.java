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

import java.util.concurrent.Executor;

import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.materializer.ena.executor.SimpleExecutor;
import uk.ac.ebi.proteome.services.ServiceContext;

/**
 * Delegating processor which will decorate a genome with UPI xrefs and then
 * Interpro domains, GO terms and UniProt xrefs
 * 
 * @author dstaines
 * 
 */
public class EnaGenomeProcessor extends DelegatingGenomeProcessor {

	public EnaGenomeProcessor(EnaGenomeConfig config, ServiceContext context) {
		this(config, context, new XmlDatabaseReferenceTypeRegistry(),
				new SimpleExecutor());
	}

	public EnaGenomeProcessor(EnaGenomeConfig config, ServiceContext context,
			Executor executor) {
		this(config, context, new XmlDatabaseReferenceTypeRegistry(), executor);
	}

	public EnaGenomeProcessor(EnaGenomeConfig config, ServiceContext context,
			DatabaseReferenceTypeRegistry registry, Executor executor) {
		super(
				new ComponentSortingProcessor(config),
				new ConXrefProcessor(config, context, registry),
				new PubMedCentralProcessor(registry),
				new LocationOverlapProcessor(config, registry),
				new LocusTagMergeProcessor(config, registry),
				new AltTranslationProcessor(config, registry),
				new UpiGenomeProcessor(config, context, registry),
				new UniProtGenomeProcessor(config, context, registry),
				new UniProtDescriptionGenomeProcessor(config, context, registry),
				new UniProtXrefGenomeProcessor(config, context, registry),
				new UniProtECGenomeProcessor(config, context, registry),
				new UpiInterproGenomeProcessor(config, context, registry),
				new InterproPathwayGenomeProcessor(config, context, registry),
				new AssemblyContigProcessor(config, registry, executor),
				new RfamProcessor(config, context, registry));

		if (config.isUseAccessionsForNames()) {
			addProcessor(new ComponentAccessionNamingProcessor());
		}

	}

}
