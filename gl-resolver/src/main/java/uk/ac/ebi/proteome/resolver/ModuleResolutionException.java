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
 * File: ModuleResolutionException.java
 * Created by: dstaines
 * Created on: Feb 8, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

import uk.ac.ebi.proteome.services.ServiceException;

/**
 * Exception thrown by resolver classes
 * 
 * @author dstaines
 * 
 */
public class ModuleResolutionException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param arg0
	 */
	public ModuleResolutionException(ServiceException arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ModuleResolutionException(String arg0, int arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ModuleResolutionException(String arg0, ServiceException arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public ModuleResolutionException(String arg0, Throwable arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ModuleResolutionException(Throwable arg0, int arg1) {
		super(arg0, arg1);
	}

}
