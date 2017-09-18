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
 * File: RankedCompoundRichLocation.java
 * Created by: dstaines
 * Created on: Jun 24, 2008
 * CVS:  $$
 */
package org.ensembl.genomeloader.util.biojava;

import java.util.Collection;

import org.biojavax.bio.seq.CompoundRichLocation;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.ontology.ComparableTerm;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * @author dstaines
 *
 */
public class RankedCompoundRichLocation extends CompoundRichLocation {

	/**
	 *
	 */
	public RankedCompoundRichLocation() {
		super();
		setMinMax();
	}

	/**
	 * @param members
	 */
	public RankedCompoundRichLocation(Collection members) {
		super(members);
		setMinMax();
	}

	/**
	 * @param term
	 * @param members
	 */
	public RankedCompoundRichLocation(ComparableTerm term, Collection members) {
		super(term, members);
		setMinMax();
	}

	protected void setMinMax() {
		// biojava conviently "loses" the circular location when
		// constructing
		RichLocation firstElem = (RichLocation) (CollectionUtils
				.getFirstElement(members, null));
		RichLocation lastElem = (RichLocation) (CollectionUtils.getLastElement(
				members, null));
		setCircularLength(firstElem.getCircularLength());
		setMinPosition(firstElem.getMinPosition());
		setMaxPosition(lastElem.getMaxPosition());
	}

}
