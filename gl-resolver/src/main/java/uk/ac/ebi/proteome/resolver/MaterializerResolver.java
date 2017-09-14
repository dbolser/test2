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
 * File: ComponentResolver.java
 * Created by: dstaines
 * Created on: Jan 25, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

/**
 * Interface specifying methods for materializing a ComponentMaterializer
 * appropriate to the task and component
 *
 * @author dstaines
 */
public interface MaterializerResolver<K,V extends EntityMetaData> {

	/**
	 *
	 * @param entityMetaData
	 * @param task
	 * @return
	 * @throws ModuleResolutionException
	 */
	public abstract MaterializerModule<K, V> getMaterializer(
			V entityMetaData, TaskDefinition task)
			throws ModuleResolutionException;

}
