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

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.genomebuilder.model.CrossReferenced;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Identifiable;
import uk.ac.ebi.proteome.genomebuilder.model.ModelUtils;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.genomebuilder.model.RepeatRegion;
import uk.ac.ebi.proteome.genomebuilder.model.RnaTranscript;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;

/**
 * Add location based identifiers for features
 * 
 * @author dstaines
 *
 */
public class LocationIdProcessor implements GenomeProcessor {
	private final DatabaseReferenceType enaFeatureId;

	public LocationIdProcessor(DatabaseReferenceTypeRegistry registry) {
		enaFeatureId = registry.getTypeForName("ENA_FEATURE");
	}

	protected void setId(Identifiable id) {
		if (StringUtils.isEmpty(id.getIdentifyingId())) {
			final DatabaseReference ref = ModelUtils.getReferenceForType(
					(CrossReferenced) id, enaFeatureId);
			if (ref != null) {
				id.setIdentifyingId(ref.getPrimaryIdentifier());
			}
		}
	}

	public void processGenome(Genome genome) {
		for (final GenomicComponent component : genome.getGenomicComponents()) {
			for (final Gene gene : component.getGenes()) {
				setId(gene);
				for (final Protein protein : gene.getProteins()) {
					setId(protein);
					for (final Transcript transcript : protein.getTranscripts()) {
						setId(transcript);
					}
				}
			}
			for (final Pseudogene gene : component.getPseudogenes()) {
				setId(gene);
			}
			for(final Rnagene gene: component.getRnagenes()) {
				setId(gene);
				for (final RnaTranscript transcript : gene.getTranscripts()) {
					setId(transcript);
				}
			}
			for(final RepeatRegion repeat: component.getRepeats()) {
				setId(repeat);
			}

		}
	}

}
