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

package uk.ac.ebi.proteome.services.support.propertylocators;

import java.util.HashMap;
import java.util.Map;

/**
 * Backs the current instance with a default map instance which can be
 * populated at will
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class MapPropertyLocator<K, V> extends AbstractPropertyLocator<K, V> {

	private Map<K,V> map = null;

	/**
	 * Initalises an empty object
	 */
	public MapPropertyLocator() {
		map = new HashMap<K, V>();
	}

	/**
	 * Initalises the object to the given values
	 */
	public MapPropertyLocator(Map<K,V> properties) {
		this();
		setProperties(properties);
	}

	@Override
	protected V provideProperty(K key) {
		return map.get(key);
	}

	public void setProperty(K key, V value) {
		map.put(key, value);
	}
	
	@Override
	public Boolean hasProperty(K key) {
		return map.containsKey(key);
	}

	/**
	 * Whilst not an official member of the interface this is here to aid
	 * programming
	 */
	public void setProperties(Map<K,V> properties) {
		map.putAll(properties);
	}
}
