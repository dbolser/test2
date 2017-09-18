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
 * File: Component.java
 * Created by: dstaines
 * Created on: Mar 8, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.model;

import java.util.List;
import java.util.Set;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.sequence.Sequence;

/**
 * A genomic component represents a molecule (or operational representation
 * thereof) that belongs to a genome, i.e. a chromosome or a plasmid.
 * Effectively represents a row in proteome.component
 *
 * @author dstaines
 */
public interface GenomicComponent extends Integr8ModelComponent, CrossReferenced {

	public static final String GENOMIC_COMPONENT = "GENOMIC_COMPONENT";

	public static final String DATA_TYPE = "genomicComponent";

	/**
	 * Register a gene as a pseudogene
	 *
	 * @param gene
	 *            pseudogene to add
	 */
	public void addPseudogene(Pseudogene gene);

	public void addGene(Gene gene);

	/**
	 * @return principal datasource identifier for the component
	 */
	public String getAccession();

	/**
	 * Retrieve set of genes for this component
	 *
	 * @return set of genes for this component
	 */
	public Set<Gene> getGenes();

	/**
	 * Retrieve set of pseudogenes
	 *
	 * @return set of pseudogenes
	 */
	public Set<Pseudogene> getPseudogenes();

	/**
	 * @return additional metadata concerning component
	 */
	public GenomicComponentMetaData getMetaData();
	
	/**
	 * @return stable ID for component
	 */
	public String getId();

	/**
	 * @return genomic sequence for component
	 */
	public Sequence getSequence();
	
	public void setSequence(Sequence sequence);

	/**
	 * @return genome to which this belongs
	 */
	public Genome getGenome();
	
	public Set<RepeatRegion> getRepeats();

	public Set<Rnagene> getRnagenes();
	
	public Set<SimpleFeature> getFeatures();
	
	public boolean isTopLevel();
	
	public List<AssemblyElement> getAssemblyElements();

	public String getVersionedAccession();

}
