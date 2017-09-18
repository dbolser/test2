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

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData.GenomicComponentType;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidationException;
import uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidator;

/**
 * Validator that checks the genome has some genes
 * 
 * @author dstaines
 * 
 */
public class GeneCountValidator implements GenomeValidator {

	private final int minGenes;

	public GeneCountValidator(int minGenes) {
		this.minGenes = minGenes;
	}

	public void validateGenome(Genome genome) throws GenomeValidationException {
		int count = 0;
		for (final GenomicComponent component : genome.getGenomicComponents()) {
			if (component.getMetaData().getComponentType() == GenomicComponentType.CHROMOSOME
					|| component.getMetaData().getComponentType() == GenomicComponentType.SUPERCONTIG) {
				count += component.getGenes().size();
				count += component.getRnagenes().size();
			}
		}
		if (count < minGenes) {
			throw new GenomeValidationGeneCountException("Genome "
					+ genome.getName() + " has less than " + minGenes
					+ " genes");
		}
	}

}
