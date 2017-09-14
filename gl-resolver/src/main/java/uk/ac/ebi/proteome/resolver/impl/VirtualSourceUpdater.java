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
 * File: VirtualSourceUpdater.java
 * Created by: dstaines
 * Created on: Jul 5, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver.impl;

import uk.ac.ebi.proteome.services.version.VersionService;

/**
 * Interface for generation of virtual sources in the {@link VersionService}
 * using multiple underlying data sources
 *
 * @author dstaines
 *
 */
public interface VirtualSourceUpdater {

	/**
	 * Method to assemble a list of "virtual" sections from the underlying
	 * sources
	 */
	public abstract void update();

}
