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
 * File: AbstractPropertyLocator.java
 * Created by: mhaimel
 * Created on: 4 Feb 2008
 * CVS: $Id$
 */
package uk.ac.ebi.proteome.services.support.propertylocators;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.services.PropertyLocator;

/**
 * Provides basic functionality and standard behaviour for different implementations.
 * 
 * @author mhaimel
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractPropertyLocator<K, V> implements PropertyLocator<K, V> {

	
	/**
	 * Provides the Property for the key. An empty property 
	 * should be able to be identified by {@link #isEmpty(Object)}
	 * 
	 * @param key
	 * @return Value of the property
	 */
	protected abstract V provideProperty(K key);
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	public V getProperty(K key, V defaultValue) {
		V value = provideProperty(key);
		if(isEmpty(value)) {
			value = defaultValue;
		}
		return value;
	}
	
	/**
	 * 
	 * Delegates to {@link #getProperty(Object, Object)} 
	 * with NULL as default value to have a standard behaviour
	 * 
	 * {@inheritDoc}
	 * 
	 */
	public V getProperty(K key) {
		return getProperty(key, null);
	}

	/**
	 * Check, if a Value is Empty 
	 * 
	 * @param value
	 * @return True, if the value is null or an empty string
	 */
	protected boolean isEmpty(V value) {
		return null == value || 
					StringUtils.isEmpty(
						String.valueOf(value));
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public Boolean hasProperty(K key) {
		return getProperty(key,null) != null;
	}
	
}
