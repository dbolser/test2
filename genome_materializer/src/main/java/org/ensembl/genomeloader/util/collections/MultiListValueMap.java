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
package org.ensembl.genomeloader.util.collections;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation backed by a sorted set
 *
 * @author dstaines
 *
 */
public class MultiListValueMap<K, V> extends MultiValueMap<K, V, List<V>> {

	public MultiListValueMap() {
		super();
	}

	public MultiListValueMap(int initialCapacity) {
		super(initialCapacity);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.hamap.impl.remodeller.MultiValueMap#getValueCollection()
	 */
	@Override
	protected List<V> getValueCollection() {
		return new ArrayList<V>();
	}

}
