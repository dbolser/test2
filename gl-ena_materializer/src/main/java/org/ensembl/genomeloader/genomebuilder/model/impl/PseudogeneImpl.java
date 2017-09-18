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
 * File: PseudogeneImpl.java
 * Created by: dstaines
 * Created on: Nov 29, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.model.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ensembl.genomeloader.genomebuilder.model.AnnotatedGene;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReference;
import org.ensembl.genomeloader.genomebuilder.model.EntityLocation;
import org.ensembl.genomeloader.genomebuilder.model.GeneName;
import org.ensembl.genomeloader.genomebuilder.model.GeneNameType;
import org.ensembl.genomeloader.genomebuilder.model.Pseudogene;

/**
 * Concrete implementation of Pseudogene which should be constructed from a Gene
 * and a Location
 * 
 * @author dstaines
 * 
 */
public class PseudogeneImpl implements Pseudogene {

	private PseudogeneType type = PseudogeneType.EMBL_ANNOTATION;

	private Set<DatabaseReference> databaseReferences;

	private String description;

	/**
	 * gene names
	 */
	private final AnnotatedGene gene;
	/**
	 * location of pseudogene
	 */
	private EntityLocation location;

	private static final long serialVersionUID = 8131779527997126008L;

	/**
	 * @param gene
	 * @param location
	 */
	public PseudogeneImpl(AnnotatedGene gene, EntityLocation location) {
		this.location = location;
		this.gene = gene;
	}

	public PseudogeneImpl(AnnotatedGene gene) {
		this.gene = gene;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.AnnotatedGene#addAnnotatedGene
	 * (org.ensembl.genomeloader.genomebuilder.model.AnnotatedGene)
	 */
	public void addAnnotatedGene(AnnotatedGene annotatedGene) {
		gene.addAnnotatedGene(annotatedGene);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.AnnotatedGene#addGeneName(uk.ac
	 * .ebi.proteome.genomebuilder.model.GeneNameType, java.lang.String)
	 */
	public void addGeneName(GeneName name) {
		gene.addGeneName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#getIdString
	 * ()
	 */
	public String getIdString() {
		return name + ":" + gene.getIdString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.AnnotatedGene#getNameCount()
	 */
	public int getNameCount() {
		return gene.getNameCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.AnnotatedGene#getNameMap()
	 */
	public Map<GeneNameType, List<GeneName>> getNameMap() {
		return gene.getNameMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.Locatable#getLocation()
	 */
	public EntityLocation getLocation() {
		return location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Locatable#setLocation(uk.ac.ebi
	 * .proteome.genomebuilder.model.EntityLocation)
	 */
	public void setLocation(EntityLocation location) {
		this.location = location;
	}

	private String name = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.Pseudogene#getName()
	 */
	public String getName() {
		if (name == null) {
			if (gene != null
					&& gene.getNameMap().get(GeneNameType.NAME).size() > 0) {
				name = gene.getNameMap().get(GeneNameType.NAME).iterator()
						.next().getName();
			}
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Pseudogene#setName(java.lang.String
	 * )
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.CrossReferenced#addDatabaseReference
	 * (org.ensembl.genomeloader.genomebuilder.model.DatabaseReference)
	 */
	public void addDatabaseReference(DatabaseReference reference) {
		getDatabaseReferences().add(reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.CrossReferenced#getDatabaseReferences
	 * ()
	 */
	public Set<DatabaseReference> getDatabaseReferences() {
		if (databaseReferences == null) {
			databaseReferences = new HashSet<DatabaseReference>();
		}
		return databaseReferences;
	}

	private String identifyingId;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Identifiable#getIdentifyingId()
	 */
	public String getIdentifyingId() {
		return identifyingId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Identifiable#setIdentifyingId(
	 * java.lang.String)
	 */
	public void setIdentifyingId(String identifyingId) {
		this.identifyingId = identifyingId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.Pseudogene#getType()
	 */
	public PseudogeneType getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Pseudogene#setType(org.ensembl.genomeloader
	 * .genomebuilder.model.Pseudogene.PseudogeneType)
	 */
	public void setType(PseudogeneType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
