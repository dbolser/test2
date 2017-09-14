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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojavax.bio.seq.RichLocation;

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.ModelUtils;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Processor that identifies genes which share the same locus tag and attempts
 * to merge them (either as origin-split genes, or as alternative transcripts)
 * 
 * @author dstaines
 * 
 */
public class LocusTagMergeProcessor implements GenomeProcessor {

	private Log log;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	public LocusTagMergeProcessor(EnaGenomeConfig config,
			DatabaseReferenceTypeRegistry registry) {
	}

	public void processGenome(Genome genome) {
		for (GenomicComponent component : genome.getGenomicComponents()) {
			// 1. find genes with the same locus tag in each component
			Map<String, List<Gene>> dupGenes = CollectionUtils.createHashMap();
			for (Gene gene : component.getGenes()) {
				if (!StringUtils.isEmpty(gene.getIdentifyingId())) {
					List<Gene> dups = dupGenes.get(gene.getIdentifyingId());
					if (dups == null) {
						dups = CollectionUtils.createArrayList();
						dupGenes.put(gene.getIdentifyingId(), dups);
					}
					dups.add(gene);
				}
			}
			// 2. for each gene set
			for (Entry<String, List<Gene>> e : dupGenes.entrySet()) {
				List<Gene> genes = e.getValue();
				if (genes.size() > 1) {
					getLog().info(
							"Processing duplicate " + genes.size()
									+ " genes with tag " + e.getKey()
									+ " from component "
									+ component.getAccession());
					Collections.sort(genes,
							new ModelUtils.LocatableComparatorMinOnly());
					if (!ModelUtils.featuresSameStrand(genes)) {
						getLog().warn(
								"Found "
										+ genes.size()
										+ " genes with the locus tag "
										+ e.getKey()
										+ " from component "
										+ component.getAccession()
										+ " but could not merge them as they are on different strands");
						// resetGeneIds(genes);
					} else {
						// Option 1 - two genes, either side of the origin
						// - Action: discard one, stretch the other
						if (!mergeSplit(component, genes)) {
							// Option 2: - more than one overlapping genes
							// - Action: find outer bounds and merge into one
							// gene
							if (!mergeSpliced(component, genes)) {
								getLog().warn(
										"Found " + genes.size()
												+ " genes with the locus tag "
												+ e.getKey()
												+ " from component "
												+ component.getAccession()
												+ " but could not merge them");
								//resetGeneIds(genes);
							} else {
								getLog().info(
										"Merged " + genes.size()
												+ " genes with tag "
												+ e.getKey()
												+ " from component "
												+ component.getAccession());
							}
						} else {
							getLog().info(
									"Merged " + genes.size()
											+ " origin-split genes with tag "
											+ e.getKey());
						}
					}
				}
			}
		}
	}

	protected void resetGeneIds(List<Gene> genes) {
		int n = 0;
		for (Gene g : genes) {
			g.setIdentifyingId(g.getIdentifyingId() + "_" + ++n);
		}
	}

	/**
	 * If possible, merge two genes which sit either side of the origin. This
	 * code assumes two simple CDSs with a single gene-protein-transcript troika
	 * 
	 * @param component
	 *            component to which genes belong
	 * @param genes
	 *            list of genes to merge
	 * @return true if genes merged
	 */
	protected boolean mergeSplit(GenomicComponent component, List<Gene> genes) {
		boolean merged = false;
		// only applicable for a pair of genes
		if (genes.size() == 2) {
			// sort by start
			Gene gene1 = genes.get(0); // gene at start
			Gene gene2 = genes.get(1); // gene at end
			EntityLocation gloc1 = gene1.getLocation();
			EntityLocation gloc2 = gene2.getLocation();
			if (gloc1.getMin() == 1
					&& gloc2.getMax() == component.getMetaData().getLength()
					&& component.getMetaData().isCircular()) {
				// split so create a new location
				gene1.setLocation(mergeSplitLoc(gloc1, gloc2, component
						.getMetaData().getLength()));
				gene1.getDatabaseReferences().addAll(
						gene2.getDatabaseReferences());
				Protein p1 = CollectionUtils.getFirstElement(
						gene1.getProteins(), null);
				Protein p2 = CollectionUtils.getFirstElement(
						gene2.getProteins(), null);
				EntityLocation ploc1 = p1.getLocation();
				EntityLocation ploc2 = p2.getLocation();
				RichLocation newPloc = LocationUtils.buildCircularLocation(
						ploc1.getMax(), ploc2.getMin(), component.getMetaData()
								.getLength(), ploc1.getStrand());
				p1.setLocation(new DelegatingEntityLocation(newPloc));
				p1.getDatabaseReferences().addAll(p2.getDatabaseReferences());
				Transcript t1 = CollectionUtils.getFirstElement(
						p1.getTranscripts(), null);
				Transcript t2 = CollectionUtils.getFirstElement(
						p2.getTranscripts(), null);
				EntityLocation tloc1 = t1.getLocation();
				EntityLocation tloc2 = t2.getLocation();
				t1.setLocation(mergeSplitLoc(tloc1, tloc2, component
						.getMetaData().getLength()));
				t1.getDatabaseReferences().addAll(t2.getDatabaseReferences());
				// ditch gene2
				component.getGenes().remove(gene2);
				merged = true;
			}
		}
		return merged;
	}

	protected DelegatingEntityLocation mergeSplitLoc(EntityLocation gloc1,
			EntityLocation gloc2, int length) {
		RichLocation loc = LocationUtils.buildLocation(gloc2.getMin(),
				gloc1.getMax(), length, gloc1.getStrand(), null);
		loc.setCircularLength(length);
		return new DelegatingEntityLocation(loc);
	}

	/**
	 * If possible, merge two genes which sit either side of the origin. This
	 * code assumes two simple CDSs with a single gene-protein-transcript troika
	 * 
	 * @param component
	 *            component to which genes belong
	 * @param genes
	 *            list of genes to merge
	 * @return true if genes merged
	 */
	protected boolean mergeSpliced(GenomicComponent component, List<Gene> genes) {
		boolean merged = false;
		boolean overlaps = true;		
		for (int i = 0; i < genes.size(); i++) {
			boolean geneOverlaps = false;
			Gene gene1 = genes.get(i);
			for (int j = 0; j < genes.size(); j++) {
				Gene gene2 = genes.get(j);
				if (gene1!=gene2 && gene1.getLocation()
						.overlaps(gene2.getLocation())) {
					geneOverlaps = true;
					break;
				}
			}
			if(!geneOverlaps) {
				overlaps = false;
				break;
			}
		}
		
		if (overlaps) {
			// merge the overlapping set into one
			Gene g = genes.get(0);
			int min = g.getLocation().getMin();
			int max = g.getLocation().getMax();
			for (int i = 1; i < genes.size(); i++) {
				Gene g2 = genes.get(i);
				g.getDatabaseReferences().addAll(g2.getDatabaseReferences());
				g.getProteins().addAll(g2.getProteins());
				min = g2.getLocation().getMin() < min ? g2.getLocation()
						.getMin() : min;
				max = g2.getLocation().getMax() > max ? g2.getLocation()
						.getMax() : max;
				component.getGenes().remove(g2);
			}
			// adjust the outer bounds of the gene if needed
			if (min != g.getLocation().getMin()
					|| max != g.getLocation().getMax()) {
				g.setLocation(new DelegatingEntityLocation(LocationUtils
						.buildLocation(min, max, g.getLocation()
								.getCircularLength(), g.getLocation()
								.getStrand(), null)));
			}
			merged = true;
		}
		return merged;
	}

}
