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

package uk.ac.ebi.proteome.services;


/**
 * Used as a way of locating properties in an implementation aganostic manner.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public interface PropertyLocator<K,V> {

	/**
	 * Returns a value for a given key after consulting a number of
	 * property holding resources (this can be anything so long as it can
	 * return a String value for a String key).
	 *
	 * @param key The property to look for
	 * @param defaultValue The value to return if the property cannot be found
	 * @return The value of the key or the defaultValue. Check with the
	 * implementations for the resolution order
	 */
	V getProperty(K key, V defaultValue);

	/**
	 * Allows you to get a property which will return a null if no property
	 * can be found.
	 */
	V getProperty(K key);

	/**
	 * Sets the key value pair in the implementing object's property store
	 */
	void setProperty(K key, V value);
	
	/**
	 * Provides a check, if a property is available for the given key
	 * @param key
	 * @return Boolean True, if a property is available
	 */
	Boolean hasProperty(K key);
}
