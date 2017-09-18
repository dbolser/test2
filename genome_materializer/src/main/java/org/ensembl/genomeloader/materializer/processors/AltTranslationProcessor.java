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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.ModelUtils;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.Transcript;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

/**
 * Processor that identifies proteins belonging to the same gene that share the
 * same uniprot accession and merges the transcript
 * 
 * @author dstaines
 * 
 */
public class AltTranslationProcessor implements GenomeProcessor {

	private Log log;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	private final DatabaseReferenceType[] types;

	public AltTranslationProcessor(EnaGenomeConfig config,
			DatabaseReferenceTypeRegistry registry) {
		types = new DatabaseReferenceType[] {
				registry.getTypeForQualifiedName("UniProtKB", "Swiss-Prot"),
				registry.getTypeForQualifiedName("UniProtKB", "TrEMBL") };
	}

	public void processGenome(Genome genome) {
		for (GenomicComponent component : genome.getGenomicComponents()) {
			for (Gene gene : component.getGenes()) {
				if (gene.getProteins().size() > 1) {
					Map<String, List<Protein>> altProt = CollectionUtils
							.createHashMap();
					for (Protein p : gene.getProteins()) {
						if (p.getTranscripts().size() > 1) {
							getLog().warn(
									"Not attempting to merge multiple translations for protein "
											+ p.getIdentifyingId()
											+ " as it has multiple transcripts");
						} else {
							for (DatabaseReferenceType type : types) {
								for (DatabaseReference ref : ModelUtils
										.getReferencesForType(p, type)) {
									List<Protein> ps = altProt.get(ref
											.getPrimaryIdentifier());
									if (ps == null) {
										ps = CollectionUtils.createArrayList();
										altProt.put(ref.getPrimaryIdentifier()+"/"+p.getIdentifyingId(),
												ps);
									}
									ps.add(p);
								}
							}
						}
					}
					for (Entry<String, List<Protein>> e : altProt.entrySet()) {
						if (e.getValue().size() > 1) {
							Transcript t = null;
							for (Protein p : e.getValue()) {
								if (t == null) {
									t = CollectionUtils.getFirstElement(
											p.getTranscripts(), null);
								} else {
									Transcript ot = CollectionUtils
											.getFirstElement(
													p.getTranscripts(), null);
									getLog().info(
											"Replacing transcript "
													+ ot.getIdentifyingId()
													+ " for protein "
													+ p.getIdentifyingId()
													+ " with transcript "
													+ t.getIdentifyingId()
													+ " as the same protein product is encoded");
									t.getDatabaseReferences().addAll(
											ot.getDatabaseReferences());
									p.getTranscripts().clear();
									p.addTranscript(t);
								}
							}
						}
					}
				}
			}
		}
	}

}
