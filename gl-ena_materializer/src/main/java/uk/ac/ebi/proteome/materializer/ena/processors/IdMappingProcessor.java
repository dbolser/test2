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
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.symbol.Location;

import uk.ac.ebi.proteome.genomebuilder.model.CrossReferenced;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Identifiable;
import uk.ac.ebi.proteome.genomebuilder.model.Locatable;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.genomebuilder.model.RepeatRegion;
import uk.ac.ebi.proteome.genomebuilder.model.RnaTranscript;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.materializer.ena.NullIdentificationException;
import uk.ac.ebi.proteome.materializer.ena.identifiers.EnaIdentifierMapper;
import uk.ac.ebi.proteome.materializer.ena.identifiers.EnaIdentifierMapper.IdentifierType;
import uk.ac.ebi.proteome.materializer.ena.identifiers.impl.DatabaseBackedEnaIdentifierMapper;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.collections.DefaultingMap;

/**
 * Generate new ID for objects that lack them
 * 
 * @author dstaines
 *
 */
public class IdMappingProcessor implements GenomeProcessor {
	private static final String PROTEIN_CODING_TYPE = "GeneImpl";
	private static final String TRANSCRIPT_TYPE = "TranscriptImpl";

	private Log log;

	protected Log getLog() {
		if (log == null)
			log = LogFactory.getLog(this.getClass());
		return log;
	}

	private final EnaIdentifierMapper mapper;
	private final EnaGenomeConfig config;

	public IdMappingProcessor(DatabaseReferenceTypeRegistry registry,
			EnaGenomeConfig config, ServiceContext context) {
		this.mapper = new DatabaseBackedEnaIdentifierMapper(config, context);
		this.config = config;
	}

	protected void setId(GenomicComponent component, Identifiable id,
			Map<String, Integer> total, Map<String, Integer> mapped) {
		String key = id.getClass().getSimpleName();
		if (Gene.class.isAssignableFrom(id.getClass())) {
			if (((Gene) id).isPseudogene()) {
				key += "_pseudo";
			}
		}
		final Integer totalCount = total.get(key);
		total.put(key, totalCount + 1);
		if (StringUtils.isEmpty(id.getIdentifyingId())) {
			if (PROTEIN_CODING_TYPE.equals(key)) {
				setFromTranscript(id, mapped, key);
			}
			if (TRANSCRIPT_TYPE.equals(key)) {
				setFromTranscript(id, mapped, key);
			}
			if (StringUtils.isEmpty(id.getIdentifyingId())) {
				generateNewId(component, id, mapped, key);
			}
		}
	}

	protected void setFromTranscript(Identifiable id,
			Map<String, Integer> mapped, String key) {
		String pId = null;
		if (Gene.class.isAssignableFrom(id.getClass())) {
			final Gene g = (Gene) id;
			if (g.getProteins().size() == 1) {
				pId = CollectionUtils.getFirstElement(g.getProteins(), null)
						.getIdentifyingId();
				if (!StringUtils.isEmpty(pId)) {
					getLog().info(
							"Reusing identifier "
									+ pId
									+ " from child protein as identifier for gene");
					g.setIdentifyingId(pId);
					final Integer mapCount = mapped.get(key);
					mapped.put(key, mapCount + 1);
					pId = null;
				}
			}
		} else if (Transcript.class.isAssignableFrom(id.getClass())) {
			final Transcript t = (Transcript) id;
			if (t.getProteins().size() == 1) {
				pId = CollectionUtils.getFirstElement(t.getProteins(), null)
						.getIdentifyingId();
			}
		}

		if (!StringUtils.isEmpty(pId)
				&& !pId.startsWith(EnaIdentifierMapper.ENA_ID_STEM)) {
			getLog().info(
					"Reusing identifier " + pId
							+ " from associated protein as identifier");
			id.setIdentifyingId(pId);
			final Integer mapCount = mapped.get(key);
			mapped.put(key, mapCount + 1);
		}

	}

	protected void generateNewId(GenomicComponent component, Identifiable id,
			Map<String, Integer> mapped, String key) {
		String idStr = null;
		final IdentifierType type = IdentifierType.typeForClass(id.getClass());
		for (final DatabaseReference r : ((CrossReferenced) id)
				.getDatabaseReferences()) {
			if (r.getDatabaseReferenceType().getDbName()
					.equalsIgnoreCase("ENA_FEATURE")) {
				idStr = r.getPrimaryIdentifier();
				break;
			}
		}
		if (StringUtils.isEmpty(idStr)) {

			// try to find if we can associate this with Rfam
			final Location loc = DelegatingEntityLocation
					.getLocation(((Locatable) id).getLocation());
			String rId = getRfamId(id);
			if (!StringUtils.isEmpty(rId)) {
				idStr = rId + ":" + component.getVersionedAccession() + ":"
						+ loc.toString();
			}

		}

		if (!StringUtils.isEmpty(idStr)) {

			if (type == null) {
				getLog().warn(
						"Could not find IdentifierType for object of class "
								+ id.getClass().getSimpleName());
			} else {
				final String newId = mapper.mapIdentifier(type, idStr);
				if (StringUtils.isEmpty(newId)) {
					getLog().warn(
							"Could not find identifier for " + id.getClass()
									+ " using identifier " + idStr);
				} else {
					final Integer mapCount = mapped.get(key);
					mapped.put(key, mapCount + 1);
					getLog().info(
							"Assigning new identifier " + newId + " to " + type
									+ " feature " + idStr);
					id.setIdentifyingId(newId);
				}
			}
		} else {
			getLog().warn(
					"Could not find suitable identifier for object of type "
							+ id.getClass());
		}
	}

	/**
	 * helper method to find the Rfam ID for a gene or from the gene to which a
	 * transcript belongs so we can use it as part of a feature key
	 * 
	 * @param id
	 * @return
	 */
	private String getRfamId(Identifiable id) {
		String rId = null;
		if (RnaTranscript.class.isAssignableFrom(id.getClass())) {
			id = ((RnaTranscript) id).getGene();
		}
		for (final DatabaseReference r : ((CrossReferenced) id)
				.getDatabaseReferences()) {
			if (r.getDatabaseReferenceType().getDbName()
					.equalsIgnoreCase("RFAM")) {
				if (RnaTranscript.class.isAssignableFrom(id.getClass())) {
					rId = r.getPrimaryIdentifier() + "_transcript";
				} else {
					rId = r.getPrimaryIdentifier();
				}
				break;
			}
		}
		return rId;
	}

	public void processGenome(Genome genome) {
		final Map<String, Integer> total = new DefaultingMap<String, Integer>(0);
		final Map<String, Integer> replaced = new DefaultingMap<String, Integer>(
				0);
		for (final GenomicComponent component : genome.getGenomicComponents()) {
			if (component.isTopLevel()) {
				getLog().info(
						"Processing genes for " + component.getAccession());
				for (final Gene gene : component.getGenes()) {
					setId(component, gene, total, replaced);
					for (final Protein protein : gene.getProteins()) {
						setId(component, protein, total, replaced);
						for (final Transcript transcript : protein
								.getTranscripts()) {
							setId(component, transcript, total, replaced);
						}
					}
				}
				getLog().info(
						"Processing pseudogenes for "
								+ component.getAccession());
				for (final Pseudogene gene : component.getPseudogenes()) {
					setId(component, gene, total, replaced);
				}
				getLog().info(
						"Processing RNA genes for " + component.getAccession());
				for (final Rnagene gene : component.getRnagenes()) {
					setId(component, gene, total, replaced);
					for (final RnaTranscript t : gene.getTranscripts()) {
						setId(component, t, total, replaced);
					}
				}
				getLog().info(
						"Processing repeats for " + component.getAccession());
				for (final RepeatRegion repeat : component.getRepeats()) {
					getLog().info("Processing repeat " + repeat.toString());
					setId(component, repeat, total, replaced);
				}
				getLog().info("Completed " + component.getAccession());
			}
		}

		final double threshold = getThreshold(genome);

		// check totals
		for (final Entry<String, Integer> e : total.entrySet()) {
			final String type = e.getKey();
			final int rep = replaced.get(type);
			final int tot = e.getValue();
			getLog().info("Replaced " + rep + " of " + tot + " " + type);
			if (PROTEIN_CODING_TYPE.equals(type)
					&& ((rep * 1.0 / tot) > threshold) && rep != tot) {
				throw new NullIdentificationException(
						rep
								+ " of "
								+ tot
								+ " "
								+ type
								+ " with null identifiers exceeds the permitted threshold of "
								+ threshold);
			}
		}
		getLog().info("Completed processing " + genome.getIdString());
	}

	protected double getThreshold(Genome genome) {
		if (genome.getCreationDate() != null
				&& genome.getCreationDate().after(config.getStrictDate())) {
			return config.getNullCdsTagThreshold();
		} else {
			return 1.0;
		}
	}

}
