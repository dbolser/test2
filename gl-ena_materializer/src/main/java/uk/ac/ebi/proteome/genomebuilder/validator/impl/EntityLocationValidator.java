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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichLocation.Strand;

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationException;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationInsertion;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationModifier;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Integr8ModelComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidationException;
import uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidator;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;

/**
 * {@link GenomeValidator} for checking if applied location modifiers are valid
 * and locations on linear components are correct
 * 
 * @author dstaines
 * 
 */
public class EntityLocationValidator implements GenomeValidator {

	private Log log;

	protected Log getLog() {
		if (log == null)
			log = LogFactory.getLog(this.getClass());
		return log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidator#validateGenome
	 * (uk.ac.ebi.proteome.genomebuilder.model.Genome)
	 */
	public void validateGenome(Genome genome) throws GenomeValidationException {
		for (GenomicComponent component : genome.getGenomicComponents()) {
			for (Gene gene : component.getGenes()) {
				for (Protein protein : gene.getProteins()) {
					EntityLocation location = protein.getLocation();
					validateLocation(protein, location, component.getMetaData()
							.isCircular());
				}
			}
		}
	}

	/**
	 * Check whether the location has correct start/end and whether all
	 * modifications are valid
	 * 
	 * @param entity
	 *            biological entity to check (e.g. Protein)
	 * @param location
	 *            location of entity
	 * @param isCircular
	 *            true if component is circular
	 * @throws GenomeValidationException
	 */
	protected static void validateLocation(Integr8ModelComponent entity,
			EntityLocation location, boolean isCircular)
			throws GenomeValidationException {
		if (!isCircular) {
			if (location.getMin() > location.getMax()) {
				throw new GenomeValidationException("Location "
						+ location.toString()
						+ " has start>end on linear component");
			}
		}
		for (EntityLocationInsertion insertion : location.getInsertions()) {
			validateModification(insertion, entity, location);
		}
		for (EntityLocationException exception : location.getExceptions()) {
			validateModification(exception, entity, location);
		}
	}

	/**
	 * Check whether the modification lies within the location
	 * 
	 * @param modifier
	 *            location modifier to check
	 * @param entity
	 *            to check (e.g. Protein)
	 * @param location
	 *            of entity
	 * @throws GenomeValidationException
	 */
	protected static void validateModification(EntityLocationModifier modifier,
			Integr8ModelComponent entity, EntityLocation location)
			throws GenomeValidationException {
		RichLocation modLoc = LocationUtils.buildSimpleLocation(
				modifier.getStart(), modifier.getStop(),
				location.getStrand() == Strand.NEGATIVE_STRAND);
		if (!LocationUtils.contains(location, modLoc)) {
			throw new GenomeValidationException("Modifier " + modifier
					+ " for protein " + entity.getIdString()
					+ " lies outside location " + location);
		} else if (Protein.class.isAssignableFrom(entity.getClass())) {
			if (location.getStrand() == Strand.NEGATIVE_STRAND) {
				if (location.getMin() == modLoc.getMax()) {
					throw new GenomeValidationException("Modifier " + modifier
							+ " for protein " + entity.getIdString()
							+ " lies outside coding sequence for " + location);
				}
			} else {
				if (location.getMax() == modLoc.getMin()) {
					throw new GenomeValidationException("Modifier " + modifier
							+ " for protein " + entity.getIdString()
							+ " lies outside coding sequence for " + location);
				}
			}
		}
	}

}
