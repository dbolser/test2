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
 * File: OperonAnnotation.java
 * Created by: dstaines
 * Created on: Dec 3, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.model.impl;

import java.util.Set;

import org.ensembl.genomeloader.genomebuilder.model.DatabaseReference;
import org.ensembl.genomeloader.genomebuilder.model.EntityLocation;
import org.ensembl.genomeloader.genomebuilder.model.Operon;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * Class holding information about an annotated operon
 *
 * @author dstaines
 *
 */
public abstract class AbstractBaseOperon implements Operon {

	EntityLocation location;

	String name;

	String operonId;

	Set<DatabaseReference> references;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.operon.Operon#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.operon.Operon#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	public EntityLocation getLocation() {
		return location;
	}

	public void setLocation(EntityLocation location) {
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.operon.Operon#getOperonId()
	 */
	public String getOperonId() {
		return operonId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.operon.Operon#setOperonId(java.lang.String)
	 */
	public void setOperonId(String operonId) {
		this.operonId = operonId;
	}

	public String getIdString() {
		return getOperonId() + ":" + getName();
	}

	public void addDatabaseReference(DatabaseReference reference) {
		getDatabaseReferences().add(reference);
	}

	public Set<DatabaseReference> getDatabaseReferences() {
		if (references == null)
			references = CollectionUtils.createHashSet();
		return references;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(getIdString());
		s.append('[');
		s.append("location=");
		if(getLocation() == null) {
			s.append("null");	
		}
		else {
			s.append(LocationUtils.locationToEmblFormat(getLocation()));
		}
		s.append(']');
		return s.toString();
	}

}
