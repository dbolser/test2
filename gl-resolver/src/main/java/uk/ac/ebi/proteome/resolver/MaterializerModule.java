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
 * File: ComponentMaterializer.java
 * Created by: dstaines
 * Created on: Jan 25, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

import java.util.Collection;

/**
 * Interface specifying methods for materializing a component as a data object
 * 
 * @author dstaines
 */
public interface MaterializerModule<T, V extends EntityMetaData> {

	/**
	 * Return a data object representing the component
	 * 
	 * @param resolverMetaData
	 * @return
	 */
	public abstract T materializeData(V resolverMetaData)
			throws MaterializationException;

	public abstract Collection<T> materializeData(Collection<V> resolverMetaData)
			throws MaterializationException;

}
