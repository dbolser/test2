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
 * File: GeneNameImpl.java
 * Created by: dstaines
 * Created on: Aug 19, 2008
 * CVS:  $$
 */
package org.ensembl.genomeloader.genomebuilder.model.impl;

import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReference;
import org.ensembl.genomeloader.genomebuilder.model.GeneName;
import org.ensembl.genomeloader.genomebuilder.model.GeneNameType;
import org.ensembl.genomeloader.util.EqualsHelper;
import org.ensembl.genomeloader.util.HashcodeHelper;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * @author dstaines
 *
 */
public class GeneNameImpl implements GeneName {

	private static final long serialVersionUID = -6144550156579153991L;
	private final String name;
	private final GeneNameType type;
	private Set<DatabaseReference> references = CollectionUtils.createHashSet();

	public GeneNameImpl(String name, GeneNameType type) {
		this.name = name;
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.GeneName#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.GeneName#getType()
	 */
	public GeneNameType getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.CrossReferenced#addDatabaseReference(org.ensembl.genomeloader.genomebuilder.model.DatabaseReference)
	 */
	public void addDatabaseReference(DatabaseReference reference) {
		getDatabaseReferences().add(reference);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.CrossReferenced#getDatabaseReferences()
	 */
	public Set<DatabaseReference> getDatabaseReferences() {
		return references;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#getIdString()
	 */
	public String getIdString() {
		return getType().getName() + ":" + getName();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		if (EqualsHelper.classEqual(this, obj)) {
			GeneName nom = (GeneName) obj;
			if (EqualsHelper.equal(getName(), nom.getName())) {
				if (EqualsHelper.equal(getType(), nom.getType())) {
					equals = true;
				}
			}
		}
		return equals;
	}

	@Override
	public int hashCode() {
		int hash = HashcodeHelper.hash(HashcodeHelper.SEED, getName());
		hash = HashcodeHelper.hash(hash, getType());
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getIdString());
		sb.append('{');
		for(DatabaseReference ref: getDatabaseReferences()) {
			sb.append("["+ref.getIdString()+"]");
		}
		sb.append('}');
		return sb.toString();
	}



}
