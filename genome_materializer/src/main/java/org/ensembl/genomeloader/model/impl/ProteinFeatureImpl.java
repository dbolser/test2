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
 * File: ProteinFeatureImpl.java
 * Created by: dstaines
 * Created on: Nov 23, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model.impl;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.EntityLocation;
import org.ensembl.genomeloader.model.ProteinFeature;
import org.ensembl.genomeloader.model.ProteinFeatureSource;
import org.ensembl.genomeloader.model.ProteinFeatureType;
import org.ensembl.genomeloader.util.EqualsHelper;
import org.ensembl.genomeloader.util.HashcodeHelper;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * @author dstaines
 * 
 */
public class ProteinFeatureImpl implements ProteinFeature {

	private static final long serialVersionUID = 4405925211309018470L;

	private ProteinFeatureType type;

	private int start;
	private int end;
	private EntityLocation location;
	private ProteinFeatureSource source;
	private Double score;
	private String id;
	private String name;
	private Set<DatabaseReference> references;

	public ProteinFeatureImpl() {
	}

	public ProteinFeatureImpl(ProteinFeatureType type, String id) {
		this.type = type;
		this.id = id;
	}

	public ProteinFeatureImpl(ProteinFeatureType type, String id, String name,
			int start, int end, ProteinFeatureSource source) {
		this.type = type;
		this.start = start;
		this.end = end;
		this.source = source;
		this.id = id;
		this.name = name;
	}

	public void addDatabaseReference(DatabaseReference reference) {
		getDatabaseReferences().add(reference);
	}

	@Override
	public boolean equals(Object obj) {
		if (!EqualsHelper.classEqual(this, obj)) {
			return false;
		} else {
			boolean equals = true;
			ProteinFeatureImpl ft = (ProteinFeatureImpl) obj;
			if (type != ft.getType()) {
				equals = false;
			} else if (start != ft.getStart()) {
				equals = false;
			} else if (end != ft.getEnd()) {
				equals = false;
			} else if (!EqualsHelper.equal(location, ft.getLocation())) {
				equals = false;
			} else if (!EqualsHelper.equal(source, ft.getSource())) {
				equals = false;
			} else if (!EqualsHelper.equal(name, ft.getName())) {
				equals = false;
			} else if (!EqualsHelper.equal(score, ft.getScore())) {
				equals = false;
			}
			return equals;
		}
	}

	public Set<DatabaseReference> getDatabaseReferences() {
		if (references == null)
			references = CollectionUtils.createHashSet();
		return references;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#getEnd()
	 */
	public int getEnd() {
		return end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#getIdString
	 * ()
	 */
	public String getIdString() {
		return toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.Locatable#getLocation()
	 */
	public EntityLocation getLocation() {
		return this.location;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#getScore()
	 */
	public Double getScore() {
		return score;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#getSource()
	 */
	public ProteinFeatureSource getSource() {
		return source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#getStart()
	 */
	public int getStart() {
		return start;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#getType()
	 */
	public ProteinFeatureType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		int hash = HashcodeHelper.hash(HashcodeHelper.SEED, start);
		hash = HashcodeHelper.hash(hash, end);
		hash = HashcodeHelper.hash(hash, type);
		hash = HashcodeHelper.hash(hash, id);
		hash = HashcodeHelper.hash(hash, location);
		hash = HashcodeHelper.hash(hash, name);
		hash = HashcodeHelper.hash(hash, source);
		hash = HashcodeHelper.hash(hash, score);
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#setEnd(int)
	 */
	public void setEnd(int end) {
		this.end = end;
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

	public void setName(String name) {
		this.name = name;
	}

	public void setId(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#setScore(java.lang
	 * .Long)
	 */
	public void setScore(Double score) {
		this.score = score;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#setSource(uk.ac
	 * .ebi.proteome.genomebuilder.model.ProteinFeatureSource)
	 */
	public void setSource(ProteinFeatureSource source) {
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#setStart(int)
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.ProteinFeature#setType(uk.ac.ebi
	 * .proteome.genomebuilder.model.ProteinFeatureType)
	 */
	public void setType(ProteinFeatureType type) {
		this.type = type;
	}

	public String toString() {
		StringBuilder desS = new StringBuilder();
		desS.append(getId() == null ? 0 : getId());
		if (!StringUtils.isEmpty(getName())) {
			desS.append(":" + getName());
		}
		if (getScore() != null) {
			desS.append(":score=" + getScore());
		}
		return getType()
				+ "["
				+ getSource()
				+ "]"
				+ desS.toString()
				+ "("
				+ getStart()
				+ "-"
				+ getEnd()
				+ ") "
				+ (getLocation() != null ? LocationUtils
						.locationToEmblFormat(getLocation()) : "");
	}

}
