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
 * File: GenomicComponentImpl.java
 * Created by: dstaines
 * Created on: Nov 23, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.model.impl;

import java.util.List;
import java.util.Set;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.AssemblyElement;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.genomebuilder.model.RepeatRegion;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.model.SimpleFeature;
import uk.ac.ebi.proteome.genomebuilder.model.sequence.Sequence;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.reflection.ObjectRenderer;

/**
 * @author dstaines
 * 
 * 
 * 
 */
public class GenomicComponentImpl implements GenomicComponent {

	protected long id;

	protected Genome genome;

	protected String accession;

	private int length = 0;

	private int type;

	private Sequence sequence;

	private Set<Rnagene> rnagenes;

	private Set<SimpleFeature> features;

	private Set<RepeatRegion> repeats;

	private Set<DatabaseReference> references;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.genomebuilder.GenomicComponent#getComponentAc()
	 */
	public String getAccession() {
		return accession;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.genomebuilder.GenomicComponent#getType()
	 */
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	@Override
	public String toString() {
		return ObjectRenderer.objectToString(this);
		// return ReflectionToStringBuilder.reflectionToString(this,
		// ToStringStyle.MULTI_LINE_STYLE);
	}

	public void setLength(int length) {
		this.length = length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.genomebuilder.GenomicComponent#getLength()
	 */
	public int getLength() {
		return this.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.model.Integr8ModelComponent#getIdString
	 * ()
	 */
	public String getIdString() {
		return getAccession();
	}

	private GenomicComponentMetaData metaData;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent#getMetaData()
	 */
	public GenomicComponentMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(GenomicComponentMetaData metaData) {
		this.metaData = metaData;
	}

	private static final long serialVersionUID = -3148372101581815598L;
	private Set<Gene> genes = null;
	private Set<Pseudogene> pgenes = null;

	private boolean topLevel = false;

	private List<AssemblyElement> assemblyElements;

	public GenomicComponentImpl() {
		super();
	}

	/**
	 * @param accession
	 */
	public GenomicComponentImpl(String accession) {
		this.accession = accession;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent#addPseudogenes
	 * (uk.ac.ebi.proteome.genomebuilder.model.Gene)
	 */
	public void addPseudogene(Pseudogene gene) {
		getPseudogenes().add(gene);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent#getGenes()
	 */
	public Set<Gene> getGenes() {
		if (genes == null) {
			genes = CollectionUtils.createHashSet();
		}
		return genes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent#getPseudogenes()
	 */
	public Set<Pseudogene> getPseudogenes() {
		if (pgenes == null) {
			pgenes = CollectionUtils.createHashSet();
		}
		return pgenes;
	}

	/**
	 * @param genes
	 */
	public void setGenes(Set<Gene> genes) {
		this.genes = genes;
	}

	/**
	 * @param pgenes
	 */
	public void setPseudogenes(Set<Pseudogene> pgenes) {
		this.pgenes = pgenes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent#addGene(uk.ac
	 * .ebi.proteome.genomebuilder.model.Gene)
	 */
	public void addGene(Gene gene) {
		getGenes().add(gene);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	public Genome getGenome() {
		return genome;
	}

	public void setGenome(Genome genome) {
		this.genome = genome;
	}

	public Set<Rnagene> getRnagenes() {
		if (rnagenes == null) {
			rnagenes = CollectionUtils.createHashSet();
		}
		return rnagenes;
	}

	public Set<SimpleFeature> getFeatures() {
		if (features == null) {
			features = CollectionUtils.createHashSet();
		}
		return features;
	}

	public Set<RepeatRegion> getRepeats() {
		if (repeats == null) {
			repeats = CollectionUtils.createHashSet();
		}
		return repeats;
	}

	public Set<DatabaseReference> getDatabaseReferences() {
		if (references == null) {
			references = CollectionUtils.createHashSet();
		}
		return references;
	}

	public void addDatabaseReference(DatabaseReference reference) {
		this.getDatabaseReferences().add(reference);
	}

	public boolean isTopLevel() {
		return this.topLevel;
	}

	public void setTopLevel(boolean topLevel) {
		this.topLevel = topLevel;
	}

	public List<AssemblyElement> getAssemblyElements() {
		if (this.assemblyElements == null) {
			this.assemblyElements = CollectionUtils.createArrayList();
		}
		return this.assemblyElements;
	}

	public String getVersionedAccession() {
		return this.getMetaData().getVersionedAccession();
	}

}
