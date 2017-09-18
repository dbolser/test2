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
 * File: FeatureLocationNotFoundException.java
 * Created by: dstaines
 * Created on: Jan 4, 2008
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.biojava;

/**
 * 
 * @author dstaines
 */
public class FeatureLocationNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4395524861291810660L;

	public FeatureLocationNotFoundException() {
	}

	public FeatureLocationNotFoundException(String message) {
		super(message);
	}

	public FeatureLocationNotFoundException(Throwable cause) {
		super(cause);
	}

	public FeatureLocationNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
