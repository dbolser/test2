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

package uk.ac.ebi.proteome.genomebuilder.validator.impl;

import java.util.Set;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData.GenomicComponentType;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidationException;
import uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidator;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Validator that checks the genome does not mix chromosome/plasmid and
 * supercontig coord systems
 * 
 * @author dstaines
 * 
 */
public class MixedCoordSystemValidator implements GenomeValidator {

	public void validateGenome(Genome genome) throws GenomeValidationException {
		Set<GenomicComponentType> types = CollectionUtils.createHashSet(genome
				.getGenomicComponents().size());
		for (GenomicComponent component : genome.getGenomicComponents()) {
			types.add(component.getMetaData().getComponentType());
		}
		if (types.contains(GenomicComponentType.SUPERCONTIG)
				&& (types.contains(GenomicComponentType.PLASMID) || types
						.contains(GenomicComponentType.CHROMOSOME))) {
			throw new GenomeValidationMixedCoordException("Genome " + genome.getName()
					+ " mixes supercontig and chromosome/plasmid components");
		}
	}

}
