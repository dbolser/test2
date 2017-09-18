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

package org.ensembl.genomeloader.util.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides an abstraction over the defaulting map allowing you to plugin
 * any object factory instance which can create an object when you
 * attempt to retrieve a value with no associated key.
 *
 * <p>
 * N.B. This value is stored in the Map but you can override this functionality
 * in subclasses via {@link #addOnMiss(Object, Object)}.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class FactoryMap<K,V> implements Map<K,V> {

	private final Map<K,V> backingMap;
	private final ObjectFactory<V> factory;

	public FactoryMap(ObjectFactory<V> factory) {
		this(new HashMap<K,V>(), factory);
	}

	public FactoryMap(Map<K,V> inputMap, ObjectFactory<V> factory) {
		this.backingMap = inputMap;
		this.factory = factory;
	}

	public void clear() {
		backingMap.clear();
	}

	public boolean containsKey(Object key) {
		return backingMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return backingMap.containsValue(value);
	}

	public Set<Entry<K, V>> entrySet() {
		return backingMap.entrySet();
	}

	@SuppressWarnings({"unchecked"})
	public V get(Object key) {
		V value = backingMap.get(key);
		if(value == null) {
			value = factory.get();
			addOnMiss((K)key, value);
		}
		return value;
	}

	/**
	 * Used to add to this Map whenever the key missed a hit (only occurs
	 * during a get call).
	 */
	protected void addOnMiss(K key, V value) {
		backingMap.put(key, value);
	}

	public boolean isEmpty() {
		return backingMap.isEmpty();
	}

	public Set<K> keySet() {
		return backingMap.keySet();
	}

	public V put(K key, V value) {
		return backingMap.put(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		backingMap.putAll(t);
	}

	public V remove(Object key) {
		return backingMap.remove(key);
	}

	public int size() {
		return backingMap.size();
	}

	public Collection<V> values() {
		return backingMap.values();
	}

	@Override
	public int hashCode() {
		return backingMap.hashCode();
	}

	@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
	@Override
	public boolean equals(Object o) {
		return backingMap.equals(o);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
