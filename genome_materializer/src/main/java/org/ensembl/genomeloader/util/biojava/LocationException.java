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
 * File: BioJavaLocationException.java
 * Created by: dstaines
 * Created on: Oct 2, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.biojava;

import org.ensembl.genomeloader.services.ServiceUncheckedException;

/**
 * Exception thrown when processing biojava locations
 * @author dstaines
 *
 */
public class LocationException extends ServiceUncheckedException {

	private static final long serialVersionUID = 1790107366362560704L;

	public LocationException() {
	}

	/**
	 * @param message
	 */
	public LocationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public LocationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LocationException(String message, Throwable cause) {
		super(message, cause);
	}

}
