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
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.GeneName;
import org.ensembl.genomeloader.model.GeneNameType;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.reflection.ObjectRenderer;

/**
 * Bean implementation of Gene
 *
 * @author dstaines
 *
 */
public class GeneImpl extends AbstractModelComponent implements Gene {

	private static final long serialVersionUID = 5489255915232394262L;

	private EntityLocation location;

	private String uniprotKbAc;

	private String bioType;;

	private AnnotatedGeneImpl annotatedGene = new AnnotatedGeneImpl();

	private String name;

	private Set<DatabaseReference> databaseReferences;

	private Set<Protein> proteins;

	private Long publicId;
	
	private String description;

	public GeneImpl() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.Gene#addDatabaseReference(org.ensembl.genomeloader.genomebuilder.model.DatabaseReference)
	 */
	public void addDatabaseReference(DatabaseReference reference) {
		getDatabaseReferences().add(reference);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.Gene#addProtein(org.ensembl.genomeloader.genomebuilder.model.Protein)
	 */
	public void addProtein(Protein protein) {
		getProteins().add(protein);
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
	 * @see org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#getIdString()
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
	 * @see org.ensembl.genomeloader.genomebuilder.model.Gene#getLocation()
	 */
	public EntityLocation getLocation() {
		return this.location;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.Gene#getName()
	 */
	public String getName() {
		return this.name;
	}

	public Set<Protein> getProteins() {
		if (proteins == null) {
			proteins = CollectionUtils.createHashSet();
		}
		return proteins;
	}

	public String getUniprotKbAc() {
		return this.uniprotKbAc;
	}



	/**
	 * @param databaseReferences
	 *            the databaseReferences to set
	 */
	public void setDatabaseReferences(Set<DatabaseReference> databaseReferences) {
		this.databaseReferences = databaseReferences;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.Gene#setLocation(org.ensembl.genomeloader.genomebuilder.model.Location)
	 */
	public void setLocation(EntityLocation location) {
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.Gene#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void setProteins(Set<Protein> proteins) {
		this.proteins = proteins;
	}

	public void setUniprotKbAc(String uniprotKbAc) {
		this.uniprotKbAc = uniprotKbAc;
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
	 * @see org.ensembl.genomeloader.genomebuilder.model.Identifiable#getIdentifyingId()
	 */
	public String getIdentifyingId() {
		return annotatedGene.getIdentifyingId();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.Identifiable#setIdentifyingId(java.lang.String)
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
	 * @see org.ensembl.genomeloader.genomebuilder.model.Gene#setPublicId(java.lang.String)
	 */
	public void setPublicId(Long id) {
		this.publicId = id;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.genomeloader.genomebuilder.model.Gene#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description brief description of the gene
	 */
	public void setDescription(String description) {
		this.description = description;
	}

    @Override
    public String getBiotype() {
        return this.bioType;
    }
	
    public void setBiotype(String biotype) {
        this.bioType = biotype;
    }
    
	
	
}
