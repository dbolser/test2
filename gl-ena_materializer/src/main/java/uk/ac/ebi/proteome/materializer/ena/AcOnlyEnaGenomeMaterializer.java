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

package uk.ac.ebi.proteome.materializer.ena;

import java.util.concurrent.Executor;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo.OrganismNameType;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomeImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl;
import uk.ac.ebi.proteome.materializer.ena.processors.GenomeProcessor;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

public class AcOnlyEnaGenomeMaterializer extends EnaGenomeMaterializer {

	public AcOnlyEnaGenomeMaterializer(EnaGenomeConfig config) {
		super(config);
	}

	public AcOnlyEnaGenomeMaterializer(EnaGenomeConfig config,
			ServiceContext context) {
		super(config, context);
	}

	public AcOnlyEnaGenomeMaterializer(EnaGenomeConfig config,
			EnaParser parser, GenomeProcessor processor) {
		super(config, parser, processor);
	}

	public AcOnlyEnaGenomeMaterializer(EnaGenomeConfig config,
			GenomeProcessor processor) {
		super(config, processor);
	}

	public AcOnlyEnaGenomeMaterializer(EnaGenomeConfig config,
			GenomeProcessor processor, Executor executor) {
		super(config, processor, executor);
	}
	
	public AcOnlyEnaGenomeMaterializer(String string, EnaParser parser,
			GenomeProcessor processor) {
		super(string, parser, processor);
	}

	@Override
	protected void addComponent(Genome g, GenomicComponentMetaData md,
			GenomicComponentImpl c) {
		// discard supplied metadata
		g.addGenomicComponent(c);
		// check genetic code though
		if (c.getMetaData().getGeneticCode() == GenomicComponentMetaData.NULL_GENETIC_CODE) {
			setDefaultGeneticCode(g, c.getMetaData());
		}
	}

	@Override
	public Genome materializeData(GenomeMetaData genomeMetaData){
		Genome g = getGenome(genomeMetaData);
		// get the first to use the genome from it
		GenomicComponent fgc = CollectionUtils.getFirstElement(
				g.getGenomicComponents(), null);
		// id, taxId, name, superregnum, scope
		GenomeImpl gg = new GenomeImpl(fgc.getMetaData().getGenomeInfo());
		gg.getLineage().addAll(fgc.getMetaData().getGenomeInfo().getLineage());
		gg.setOrganismName(OrganismNameType.FULL, gg.getName());
		gg.setOrganismName(OrganismNameType.STRAIN, fgc.getMetaData().getGenomeInfo().getOrganismName(OrganismNameType.STRAIN));
		gg.setOrganismName(OrganismNameType.SUBSTRAIN, fgc.getMetaData().getGenomeInfo().getOrganismName(OrganismNameType.SUBSTRAIN));
		gg.setOrganismName(OrganismNameType.SEROTYPE, fgc.getMetaData().getGenomeInfo().getOrganismName(OrganismNameType.SEROTYPE));
		gg.setDescription(genomeMetaData.getDescription());
		gg.setOrganismName(OrganismNameType.SQL, genomeMetaData.getIdentifier());
		// add each component to the genome, giving it a dummy ID as we go
		long id = 0;
		genomeMetaData.getComponentMetaData().clear();
		for (GenomicComponent gc : g.getGenomicComponents()) {
			gg.addGenomicComponent(gc);
			gc.getMetaData().setGenomeInfo(fgc.getMetaData().getGenomeInfo());
			((GenomicComponentImpl) gc).setGenome(gg);
			((GenomicComponentImpl) gc).setId(++id);
			genomeMetaData.getComponentMetaData().add(gc.getMetaData());
			if(gg.getCreationDate()==null || gc.getMetaData().getCreationDate().before(gg.getCreationDate())) {
				gg.setCreationDate(gc.getMetaData().getCreationDate());
			}
			if(gg.getUpdateDate()==null || gc.getMetaData().getUpdateDate().after(gg.getUpdateDate())) {
				gg.setUpdateDate(gc.getMetaData().getUpdateDate());
			}
			gc.getMetaData().setComponentType(null);
			gc.getMetaData().parseComponentDescription();
		}
		genomeMetaData.setCreationDate(gg.getCreationDate());
		genomeMetaData.setUpdateDate(gg.getUpdateDate());
		genomeMetaData.setOrganismName(OrganismNameType.STRAIN, gg.getOrganismName(OrganismNameType.STRAIN));
		genomeMetaData.setOrganismName(OrganismNameType.SUBSTRAIN, gg.getOrganismName(OrganismNameType.SUBSTRAIN));
		genomeMetaData.setOrganismName(OrganismNameType.SEROTYPE, gg.getOrganismName(OrganismNameType.SEROTYPE));
		genomeMetaData.setOrganismName(OrganismNameType.SQL, genomeMetaData.getIdentifier());
		processGenome(gg);
		id = 0;
		for (GenomicComponent gc : g.getGenomicComponents()) {
			((GenomicComponentImpl) gc).setId(++id);
		}
		return gg;
	}

}
