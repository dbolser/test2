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

package org.ensembl.genomeloader.materializer.processors;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.model.CrossReferenced;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.Identifiable;
import org.ensembl.genomeloader.model.ModelUtils;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.Pseudogene;
import org.ensembl.genomeloader.model.RepeatRegion;
import org.ensembl.genomeloader.model.RnaTranscript;
import org.ensembl.genomeloader.model.Rnagene;
import org.ensembl.genomeloader.model.Transcript;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

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
