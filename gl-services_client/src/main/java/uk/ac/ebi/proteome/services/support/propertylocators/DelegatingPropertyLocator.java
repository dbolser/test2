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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.ebi.proteome.services.PropertyLocator;
import uk.ac.ebi.proteome.services.ServiceUncheckedException;

/**
 * Allows a delegating system of property location where we can loop over
 * the constructed property locators each time looking for the property.
 * Eventually the methods will return the first hit or the default value. The
 * set method will set the value into the first property locator
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class DelegatingPropertyLocator<K,V> extends AbstractPropertyLocator<K, V> {

	private List<PropertyLocator<K, V>> locators = null;

	/**
	 * Makes a local copy of the locators and creates a new object
	 */
	public DelegatingPropertyLocator(PropertyLocator<K, V> ... locators) {
		this.locators = new ArrayList<PropertyLocator<K,V>>(Arrays.asList(locators));
	}

	public void setProperty(K key, V value) {
		if(locators.size() == 0) {
			throw new ServiceUncheckedException("Length of locators was 0. " +
					"Cannot set anything");
		}
		locators.get(0).setProperty(key, value);
	}

	@Override
	protected V provideProperty(K key) {
		V value = null;
		for(PropertyLocator<K, V> locator: locators) {
			value = locator.getProperty(key);
			if(!isEmpty(value)){
				break;
			}
		}
		return value;
	}
}
