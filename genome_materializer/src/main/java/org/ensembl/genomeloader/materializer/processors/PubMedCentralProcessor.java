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
 * ConXrefProcessor
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.genomeloader.materializer.processors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.model.CrossReferenced;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.ModelUtils;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.Transcript;
import org.ensembl.genomeloader.model.impl.DatabaseReferenceImpl;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

/**
 * Processor that converts PMC xrefs to use PubMed IDs for consistency
 * 
 * @author dstaines
 * 
 */
public class PubMedCentralProcessor implements GenomeProcessor {

	private Log log;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	private final DatabaseReferenceType europePmcType;
	private final DatabaseReferenceType pubMedType;

	public PubMedCentralProcessor(DatabaseReferenceType europePmcType,
			DatabaseReferenceType pubMedType) {
		this.europePmcType = europePmcType;
		this.pubMedType = pubMedType;
	}

	public PubMedCentralProcessor(DatabaseReferenceTypeRegistry registry) {
		this(registry.getTypeForName("EuropePMC"), registry
				.getTypeForName("PubMed"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.materializer.ena.processors.GenomeProcessor#processGenome
	 * (org.ensembl.genomeloader.genomebuilder.model.Genome)
	 */
	public void processGenome(Genome genome) {
		getLog().info("Converting EuropePMC xrefs for genome " + genome.getId());
		for (GenomicComponent component : genome.getGenomicComponents()) {
			getLog().info(
					"Converting EuropePMC xrefs for component "
							+ component.getAccession());
			convertPmc(component);
			for (Gene gene : component.getGenes()) {
				convertPmc(gene);
				for (Protein protein : gene.getProteins()) {
					convertPmc(protein);
					for (Transcript transcript : protein.getTranscripts()) {
						convertPmc(transcript);
					}
				}
			}
		}
	}

	private void convertPmc(CrossReferenced referee) {
		for (DatabaseReference ref : ModelUtils.getReferencesForType(referee,
				this.europePmcType)) {
			if (!StringUtils.isEmpty(ref.getSecondaryIdentifier())) {
				referee.addDatabaseReference(new DatabaseReferenceImpl(
						this.pubMedType, ref.getSecondaryIdentifier()));
			}
		}
	}

}
