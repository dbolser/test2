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

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Identifiable;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.genomebuilder.model.RnaTranscript;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * {@link GenomeProcessor} to identify where IDs are duplicated for a given
 * region, and nullify them for future replacement
 * 
 * @author dstaines
 * 
 */
public abstract class DuplicateIdProcessor implements GenomeProcessor {

	private Log log;

	protected Log getLog() {
		if (log == null)
			log = LogFactory.getLog(this.getClass());
		return log;
	}

	protected final EnaGenomeConfig config;

	public DuplicateIdProcessor(DatabaseReferenceTypeRegistry registry,
			EnaGenomeConfig config, ServiceContext context) {
		this.config = config;
	}

	protected void add(Map<String, Set<Identifiable>> map,
			Class<? extends Identifiable> clazz, Identifiable i) {
		if (!StringUtils.isEmpty(i.getIdentifyingId())) {
			String key = clazz.getSimpleName() + "_"
					+ i.getIdentifyingId().toLowerCase();
			Set<Identifiable> ids = map.get(key);
			if (ids == null) {
				ids = CollectionUtils.createHashSet();
				map.put(key, ids);
			}
			ids.add(i);
		}
	}

	public void processGenome(Genome genome) {
		Map<String, Set<Identifiable>> dups = new CollectionUtils()
				.createHashMap();
		for (GenomicComponent gc : genome.getGenomicComponents()) {
			for (Gene g : gc.getGenes()) {
				add(dups, Gene.class, g);
				for (Protein p : g.getProteins()) {
					add(dups, Protein.class, p);
					for (Transcript t : p.getTranscripts()) {
						add(dups, Transcript.class, t);
					}
				}
			}
			for (Pseudogene g : gc.getPseudogenes()) {
				add(dups, Gene.class, g);
			}
			for (Rnagene g : gc.getRnagenes()) {
				add(dups, Gene.class, g);
				for (RnaTranscript t : g.getTranscripts()) {
					add(dups, RnaTranscript.class, t);
				}
			}
		}
		int nDups = handleDuplicates(genome, dups);
		getLog().info(
				"Processed " + nDups + " duplicates from genome "
						+ genome.getName() + " (ID " + genome.getId() + ")");
	}

	protected abstract int handleDuplicates(Genome genome,
			Map<String, Set<Identifiable>> dups);

}
