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
 * File: ComponentIdentificationException.java
 * Created by: dstaines
 * Created on: Feb 19, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

/**
 * Exception for identification of components
 * 
 * @author dstaines
 * 
 */
public class IdentificationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6736115387690710137L;

	/**
	 * 
	 */
	public IdentificationException() {
	}

	/**
	 * @param message
	 */
	public IdentificationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IdentificationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IdentificationException(String message, Throwable cause) {
		super(message, cause);
	}

}
