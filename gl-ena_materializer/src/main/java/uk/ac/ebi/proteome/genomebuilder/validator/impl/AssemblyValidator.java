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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData.GenomicComponentType;
import uk.ac.ebi.proteome.genomebuilder.model.AssemblyElement;
import uk.ac.ebi.proteome.genomebuilder.model.AssemblySequence;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidationException;
import uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidator;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Validator to ensure different levels of an assembly use different coord
 * systems
 * 
 * @author dstaines
 *
 */
public class AssemblyValidator implements GenomeValidator {

	private final Log log = LogFactory.getLog(AssemblyValidator.class);

	public void validateGenome(Genome genome) throws GenomeValidationException {
		log.info("Checking assembly for " + genome.getId());
		// generate hash map of components for looking up
		final Map<String, GenomicComponent> components = CollectionUtils
				.createHashMap();
		for (final GenomicComponent component : genome.getGenomicComponents()) {
			components.put(component.getAccession(), component);
		}
		Map<String, GenomicComponentType[]> typePairs = CollectionUtils
				.createHashMap();
		// work through components looking for assembly elements to check
		for (final GenomicComponent component : genome.getGenomicComponents()) {
			final GenomicComponentType componentType = component.getMetaData()
					.getComponentType();
			for (final AssemblyElement assElem : component
					.getAssemblyElements()) {
				if (AssemblySequence.class.isAssignableFrom(assElem.getClass())) {
					final String acc = ((AssemblySequence) assElem)
							.getAccession();
					final GenomicComponentType assType = components.get(acc)
							.getMetaData().getComponentType();
					String key = componentType.toString() + "-"
							+ assType.toString();
					if (!typePairs.containsKey(key)) {
						typePairs.put(key, new GenomicComponentType[] {
								componentType, assType });
					}
					log.debug("Checking assembly between "
							+ component.getAccession() + "(" + componentType
							+ ") and " + acc + " (" + assType + ")");
					if (assType.equals(componentType)) {
						throw new GenomeValidationException("Component "
								+ component.getAccession()
								+ " has the same type (" + componentType
								+ ") as child component " + acc);
					}
				}
			}
		}
		log.info("Checking assembly paths");
		if (typePairs.containsKey(GenomicComponentType.CHROMOSOME + "-"
				+ GenomicComponentType.CONTIG)
				&& typePairs.containsKey(GenomicComponentType.CHROMOSOME + "-"
						+ GenomicComponentType.SUPERCONTIG)) {
			String msg = "Assembly contains both chromosome-contig and chromosome-supercontig paths";
			throw new GenomeValidationException(msg);
		}
		log.info("Completed checking assembly for " + genome.getId());
	}

}
