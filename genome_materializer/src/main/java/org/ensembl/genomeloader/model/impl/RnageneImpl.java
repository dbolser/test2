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
 * File: GeneImpl.java
 * Created by: dstaines
 * Created on: Mar 9, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.model.AnnotatedGene;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.EntityLocation;
import org.ensembl.genomeloader.model.GeneName;
import org.ensembl.genomeloader.model.GeneNameType;
import org.ensembl.genomeloader.model.RnaTranscript;
import org.ensembl.genomeloader.model.Rnagene;
import org.ensembl.genomeloader.util.reflection.ObjectRenderer;

/**
 * Bean implementation of Rnagene
 *
 * @author hornead
 *
 */
public class RnageneImpl extends AbstractModelComponent implements Rnagene {
	private static final long serialVersionUID = 1L;

	private EntityLocation location;

	private boolean pseudogene;

	private AnnotatedGeneImpl annotatedGene = new AnnotatedGeneImpl();

	private String analysis;

	private String biotype;

	private String name;

	private String description;

	private Set<DatabaseReference> databaseReferences;

	private Long publicId;

	private Set<RnaTranscript> transcripts;

	public RnageneImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Gene#addDatabaseReference(uk.ac
	 * .ebi.proteome.genomebuilder.model.DatabaseReference)
	 */
	public void addDatabaseReference(DatabaseReference reference) {
		getDatabaseReferences().add(reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.Gene#getDatabaseReferences()
	 */
	public Set<DatabaseReference> getDatabaseReferences() {
		if (databaseReferences == null) {
			databaseReferences = new HashSet<DatabaseReference>();
		}
		return databaseReferences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#getIdString
	 * ()
	 */
	public String getIdString() {
		StringBuilder s = new StringBuilder();
		if (!StringUtils.isEmpty(getName())) {
			s.append(getName());
		}
		s.append(getNameMap().toString());
		return s.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Rnagene#isPseudogene()
	 */
	public void setPseudogene(boolean pseudogene) {
		this.pseudogene = pseudogene;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Gene#getName()
	 */
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Gene#isPseudogene()
	 */
	public boolean isPseudogene() {
		return this.pseudogene;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Gene#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Rnagene#getDescription()
	 */
	public String getDescription() {
		return this.description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Rnagene#setDescription()
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Rnagene#getAnalysis()
	 */
	public String getAnalysis() {
		return this.analysis;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Rnagene#setAnalysis()
	 */
	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Rnagene#getBiotype()
	 */
	public String getBiotype() {
		return this.biotype;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.Rnagene#setBiotype()
	 */
	public void setBiotype(String biotype) {
		this.biotype = biotype;
	}

	/**
	 * @param databaseReferences
	 *            the databaseReferences to set
	 */
	public void setDatabaseReferences(Set<DatabaseReference> databaseReferences) {
		this.databaseReferences = databaseReferences;
	}

	public String toString() {
		return ObjectRenderer.objectToString(this);
		// return ReflectionToStringBuilder.reflectionToString(this,
		// ToStringStyle.MULTI_LINE_STYLE);
	}

	public void addAnnotatedGene(AnnotatedGene annotatedGene) {
		this.annotatedGene.addAnnotatedGene(annotatedGene);
	}

	public void addGeneName(GeneName name) {
		annotatedGene.addGeneName(name);
	}

	public int getNameCount() {
		return annotatedGene.getNameCount();
	}

	public Map<GeneNameType, List<GeneName>> getNameMap() {
		return annotatedGene.getNameMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Identifiable#getIdentifyingId()
	 */
	public String getIdentifyingId() {
		return annotatedGene.getIdentifyingId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Identifiable#setIdentifyingId(
	 * java.lang.String)
	 */
	public void setIdentifyingId(String identifyingId) {
		annotatedGene.setIdentifyingId(identifyingId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.Gene#getPublicId()
	 */
	public Long getPublicId() {
		return publicId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Gene#setPublicId(java.lang.String)
	 */
	public void setPublicId(Long id) {
		this.publicId = id;
	}

	public EntityLocation getLocation() {
		return location;
	}

	public void setLocation(EntityLocation location) {
		this.location = location;
	}

	public Set<RnaTranscript> getTranscripts() {
		if (transcripts == null) {
			transcripts = new HashSet<RnaTranscript>();
		}
		return transcripts;
	}

	public void addTranscript(RnaTranscript transcript) {
		getTranscripts().add(transcript);
		((RnaTranscriptImpl)transcript).setGene(this);
	}

}
