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
 * File: TranssplicedCompoundRichLocation.java
 * Created by: dstaines
 * Created on: Oct 3, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.biojava;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.symbol.Location;
import org.biojavax.bio.seq.CompoundRichLocation;
import org.biojavax.bio.seq.MultiSourceCompoundRichLocation;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.ontology.ComparableTerm;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * @author dstaines
 *
 */
public class TranssplicedCompoundRichLocation extends CompoundRichLocation
		implements RichLocation {

	public TranssplicedCompoundRichLocation(MultiSourceCompoundRichLocation loc) {
		List<RichLocation> members = CollectionUtils.createArrayList();
		Iterator<RichLocation> i = loc.blockIterator();
		while(i.hasNext()) {
			members.add(i.next());
		}
		init(loc.getTerm(),members);
	}

	/**
	 * Constructs a TranssplicedCompoundRichLocation from the given set of
	 * members, with the default term of "join". The members collection must
	 * only contain Location instances. Any that are not RichLocations will be
	 * converted using RichLocation.Tools.enrich().
	 *
	 * @param members
	 *            the members to put into the compound location.
	 * @see RichLocation.Tools
	 */
	public TranssplicedCompoundRichLocation(Collection members) {
		init(getJoinTerm(), members);
	}

	/**
	 * Constructs a TranssplicedCompoundRichLocation from the given set of
	 * members. The members collection must only contain RichLocation instances.
	 * Any that are not RichLocations will be converted using
	 * RichLocation.Tools.enrich().
	 *
	 * @param term
	 *            the term to use when describing the group of members.
	 * @param members
	 *            the members to put into the compound location.
	 * @see RichLocation.Tools
	 */
	public TranssplicedCompoundRichLocation(ComparableTerm term,
			Collection members) {
		init(term, members);
	}

	protected void init(ComparableTerm term, Collection members) {
		if (term == null)
			throw new IllegalArgumentException("Term cannot be null");
		if (members == null || members.size() < 2)
			throw new IllegalArgumentException("Must have at least two members");
		this.term = term;
		this.members = new ArrayList();
		RichLocation.Strand strand = null;
		for (Iterator i = members.iterator(); i.hasNext();) {
			// Convert each member into a RichLocation
			Object o = i.next();
			if (!(o instanceof RichLocation))
				o = RichLocation.Tools.enrich((Location) o);
			// Convert
			RichLocation rl = (RichLocation) o;
			if (strand == null) {
				strand = rl.getStrand();
			} else if (strand != rl.getStrand()) {
				strand = RichLocation.Strand.UNKNOWN_STRAND;
			}
			// Add in member
			this.members.add(rl);
			// Update our size
			this.size += Math.max(rl.getMin(), rl.getMax())
					- Math.min(rl.getMin(), rl.getMax());
			if (this.getMinPosition() == null)
				this.setMinPosition(rl.getMinPosition());
			else
				this.setMinPosition(this.posmin(this.getMinPosition(), rl
						.getMinPosition()));
			if (this.getMaxPosition() == null)
				this.setMaxPosition(rl.getMaxPosition());
			else
				this.setMaxPosition(this.posmax(this.getMaxPosition(), rl
						.getMaxPosition()));
		}
		setStrand(strand);
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
