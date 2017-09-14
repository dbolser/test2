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

package uk.ac.ebi.proteome.util.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Used as a way of returning a default object when get returns null. This
 * only overrides the {@link #get(Object)} as all other methods are hardcoded
 * delegates to the backing map.
 *
 * <p>
 * Unlike the factory map this object uses a special overridden version
 * which allows it to avoid the addition of a default object and
 * causing a map size increase when none has to occur.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 * @param <K> The key type of this Map
 * @param <V> The value this map returns (must be the same as the input map)
 */
public class DefaultingMap<K, V> implements Map<K,V> {

	private final Map<K,V> backingMap;

	public DefaultingMap(V defaultObject) {
		this.backingMap = new NoAddFactoryMap<K,V>(
			new DefaultObjectFactory<V>(defaultObject));
	}

	public DefaultingMap(Map<K,V> inputMap, V defaultObject) {
		this.backingMap = new NoAddFactoryMap<K,V>(inputMap,
			new DefaultObjectFactory<V>(defaultObject));
	}

	/**
	 * Overrides {@link #addOnMiss(Object, Object)} and prevents
	 * the addition from happening.
	 */
	private static class NoAddFactoryMap<K,V> extends FactoryMap<K,V> {
		private NoAddFactoryMap(ObjectFactory<V> vObjectFactory) {
			super(vObjectFactory);
		}

		private NoAddFactoryMap(Map<K, V> inputMap, ObjectFactory<V> vObjectFactory) {
			super(inputMap, vObjectFactory);
		}

		protected void addOnMiss(K key, V value) {
			//Do nothing
		}
	}

	public int size() {
		return backingMap.size();
	}

	public boolean isEmpty() {
		return backingMap.isEmpty();
	}

	public boolean containsKey(Object key) {
		return backingMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return backingMap.containsValue(value);
	}

	public V get(Object key) {
		return backingMap.get(key);
	}

	public V put(K key, V value) {
		return backingMap.put(key, value);
	}

	public V remove(Object key) {
		return backingMap.remove(key);
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		backingMap.putAll(t);
	}

	public void clear() {
		backingMap.clear();
	}

	public Set<K> keySet() {
		return backingMap.keySet();
	}

	public Collection<V> values() {
		return backingMap.values();
	}

	public Set<Entry<K, V>> entrySet() {
		return backingMap.entrySet();
	}

	@Override
	public int hashCode() {
		return backingMap.hashCode();
	}

	@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
	@Override
	public boolean equals(Object obj) {
		return backingMap.equals(obj);
	}

	@Override
	public String toString() {
		return backingMap.toString();
	}

	/**
	 * Implementation of the factory which will always return the object
	 * used during construction.
	 */
	protected static class DefaultObjectFactory<V> implements ObjectFactory<V> {

		private final V object;

		public DefaultObjectFactory(V object) {
			this.object = object;
		}

		public V get() {
			return object;
		}
	}
}
