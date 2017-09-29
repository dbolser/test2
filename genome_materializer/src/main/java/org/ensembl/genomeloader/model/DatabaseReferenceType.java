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
 * File: DatabaseReferenceType.java
 * Created by: dstaines
 * Created on: Oct 25, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.util.EqualsHelper;
import org.ensembl.genomeloader.util.HashcodeHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Class encapsulating information about a given database cross-reference used
 * in {@link DatabaseReference}
 *
 * @author dstaines
 *
 */
public class DatabaseReferenceType implements Integr8ModelComponent {

	public static enum TypeEnum {
		GENOME, COMPONENT, GENE, PROTEIN, TRANSCRIPT, OPERON, NCRNA, FEATURE, REPEAT
	}

	private static final long serialVersionUID = -1357389131425349261L;
	@JsonIgnore
	String dbName;
    @JsonIgnore
	String displayName;
	int id;
    @JsonIgnore
	String qualifier;
    @JsonIgnore
	String uniprotKbName;
	String ensemblName;
    @JsonIgnore
	TypeEnum type;

	public DatabaseReferenceType() {
	}

	/**
	 * @param id
	 * @param dbName
	 * @param qualifier
	 * @param displayName
	 * @param uniprotKbName
	 */
	public DatabaseReferenceType(int id, String dbName, String qualifier,
			String displayName, String uniprotKbName, String ensemblName, TypeEnum type) {
		this.id = id;
		this.dbName = dbName;
		this.qualifier = qualifier;
		this.displayName = displayName;
		this.uniprotKbName = uniprotKbName;
		this.ensemblName = ensemblName;
		this.type = type;
	}

	public String getDbName() {
		return this.dbName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public int getId() {
		return this.id;
	}

	public String getQualifier() {
		return this.qualifier;
	}

	public String getUniprotKbName() {
		return this.uniprotKbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public void setUniprotKbName(String uniprotKbName) {
		this.uniprotKbName = uniprotKbName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String name = getDbName();
		if (!StringUtils.isEmpty(getDisplayName())) {
			name = getDisplayName();
		} else if (!StringUtils.isEmpty(getQualifier())) {
			name += "/" + getQualifier();
		}
		return getId() + "[" + name + "]";
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (EqualsHelper.classEqual(this, obj)) {
			result = EqualsHelper.equal(id, ((DatabaseReferenceType) obj)
					.getId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		return HashcodeHelper.hash(HashcodeHelper.SEED, id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#getIdString()
	 */
	public String getIdString() {
		return dbName;
	}

	public TypeEnum getType() {
		return type;
	}

	public void setType(TypeEnum type) {
		this.type = type;
	}

	/**
	 * @return Internal name of equivalent entry in ENSEMBL external_db table
	 */
	public String getEnsemblName() {
		return ensemblName;
	}

	public void setEnsemblName(String ensemblName) {
		this.ensemblName = ensemblName;
	}

}
