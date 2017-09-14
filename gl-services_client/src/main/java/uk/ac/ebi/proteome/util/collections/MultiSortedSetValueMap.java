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
 * File: MultiSortedSetValueMap.java
 * Created by: dstaines
 * Created on: Sep 14, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.util.collections;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Concrete implementation backed by a sorted set
 *
 * @author dstaines
 *
 */
public class MultiSortedSetValueMap<K, V> extends
		MultiValueMap<K, V, SortedSet<V>> {

	public MultiSortedSetValueMap() {
		super();
	}

	public MultiSortedSetValueMap(int initialCapacity) {
		super(initialCapacity);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.genomebuilder.hamap.impl.remodeller.MultiValueMap#getValueCollection()
	 */
	@Override
	protected SortedSet<V> getValueCollection() {
		return new TreeSet<V>();
	}

}
