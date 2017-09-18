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
 * File: MultiValueMap.java
 * Created by: dstaines
 * Created on: Aug 21, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



/**
 * Simple implementation of a map containing multiple values for each key
 *
 * @author dstaines
 *
 */
public abstract class MultiValueMap<K, V, C extends Collection<V>> {

	private Map<K, C> map;

	public MultiValueMap() {
		map = CollectionUtils.createHashMap();
	}

	public MultiValueMap(int initialCapacity) {
		map = CollectionUtils.createHashMap(initialCapacity);
	}

	public void clear() {
		this.map.clear();
	}

	public void clear(K key) {
		getValues(key).clear();
	}

	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	public Set<Entry<K, C>> entrySet() {
		return this.map.entrySet();
	}

	public Collection<V> getValues(K key) {
		return map.get(key);
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	public Set<K> keySet() {
		return this.map.keySet();
	}

	public void put(K key, V value) {
		if (!map.containsKey(key)) {
			map.put(key, getValueCollection());
		}
		map.get(key).add(value);
	}

	public void putAll(Map<K, V> inMap) {
		if (inMap != null) {
			for (Map.Entry<K, V> e : inMap.entrySet()) {
				put(e.getKey(), e.getValue());
			}
		}
	}

	public void putValues(K key, C values) {
		map.put(key, values);
	}

	public Collection<V> remove(Object key) {
		return this.map.remove(key);
	}

	public int size() {
		return this.map.size();
	}

	public Collection<C> values() {
		return this.map.values();
	}

	protected abstract C getValueCollection();

}
