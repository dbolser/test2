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
 * File: AbstractModelComponent.java
 * Created by: dstaines
 * Created on: Jul 18, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model.impl;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.ensembl.genomeloader.model.Integr8ModelComponent;

/**
 * Base class from which all components extend
 * @author dstaines
 *
 */
public abstract class AbstractModelComponent implements Integr8ModelComponent {

	@Override
	public String toString() {	    
		return ReflectionToStringBuilder.toString(this);
	}

	private String formatIdString = "%s%08d";
	
	/**
	 * Performs the formatting of the identifier string in a historical
	 * Integr8 manner
	 * 
	 * @param prefix The identifier prefix e.g. IGI
	 * @param id Identifier to format
	 * @return A string of the format PREFIX_00000001 (zero padded to 8 digits)
	 */
	protected String formatId(String prefix, Number id) {
		return String.format(formatIdString, prefix, id);
	}
}
