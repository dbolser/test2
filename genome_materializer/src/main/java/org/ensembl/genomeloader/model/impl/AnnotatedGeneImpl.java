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
 * File: AnnotatedGeneImpl.java
 * Created by: dstaines
 * Created on: Nov 23, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ensembl.genomeloader.model.AnnotatedGene;
import org.ensembl.genomeloader.model.GeneName;
import org.ensembl.genomeloader.model.GeneNameType;

/**
 * @author dstaines
 *
 */
public class AnnotatedGeneImpl implements AnnotatedGene {

	private static final long serialVersionUID = -8736808389433283708L;

	public String toString() {
		return id + ":" + getNameMap();
	}

	public String getIdString() {
		return toString();
	}

	private String id;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.AnnotatedGene#getNameCount()
	 */
	public int getNameCount() {
		int n = 0;
		for (Map.Entry<GeneNameType, List<GeneName>> e : getNameMap().entrySet()) {
			if (e.getValue() != null) {
				n += e.getValue().size();
			}
		}
		return n;
	}

	/**
	 * @param nameMap
	 *            the nameMap to set
	 */
	public void setNameMap(Map<GeneNameType, List<GeneName>> nameMap) {
		this.nameMap = nameMap;
	}

	/*
	 * (non-Javadoc)
	 *g
	 * @see org.ensembl.genomeloader.genomebuilder.AnnotatedGene#addGeneNames(org.ensembl.genomeloader.genomebuilder.AnnotatedGene)
	 */
	public void addAnnotatedGene(AnnotatedGene annotatedGene) {
		for (Entry<GeneNameType, List<GeneName>> e : annotatedGene.getNameMap()
				.entrySet()) {
			for (GeneName value : e.getValue()) {
				this.addGeneName(value);
			}
		}
	}

	private Map<GeneNameType, List<GeneName>> nameMap = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.AnnotatedGene#getNameMap()
	 */
	@SuppressWarnings("unchecked")
	public Map<GeneNameType, List<GeneName>> getNameMap() {
		if (nameMap == null) {
			nameMap = (Map<GeneNameType, List<GeneName>>) Collections.checkedMap(
					new HashMap(), GeneNameType.class, List.class);
			for (GeneNameType type : GeneNameType.values()) {
				nameMap.put(type, new ArrayList<GeneName>());
			}
		}
		return nameMap;
	}


	/* (non-Javadoc)
	 * @see org.ensembl.genomeloader.genomebuilder.model.AnnotatedGene#addGeneName(org.ensembl.genomeloader.genomebuilder.model.GeneName)
	 */
	public void addGeneName(GeneName name) {
		if (!this.getNameMap().containsKey(name.getType())) {
			this.getNameMap().put(name.getType(), new ArrayList<GeneName>());
		}
		List<GeneName> nameL = this.getNameMap().get(name.getType());
		boolean isFound = false;
		for(GeneName name2: nameL) {
			if(name2.equals(name)) {
				isFound = true;
				name2.getDatabaseReferences().addAll(name.getDatabaseReferences());
				break;
			}
		}
		if (!isFound)
			nameL.add(name);
	}

	private String identifyingId;
	/* (non-Javadoc)
	 * @see org.ensembl.genomeloader.genomebuilder.model.Identifiable#getIdentifyingId()
	 */
	public String getIdentifyingId() {
		return identifyingId;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.genomeloader.genomebuilder.model.Identifiable#setIdentifyingId(java.lang.String)
	 */
	public void setIdentifyingId(String identifyingId) {
		this.identifyingId = identifyingId;
	}


}
