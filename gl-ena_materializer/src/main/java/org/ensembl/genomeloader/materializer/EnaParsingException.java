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
 * File: EnaParsingException.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */
package org.ensembl.genomeloader.materializer;

import org.ensembl.genomeloader.genomebuilder.metadata.GenomicComponentMetaData;

/**
 * @author dstaines
 *
 */
public class EnaParsingException extends RuntimeException {

	private static final long serialVersionUID = -3708521992289760894L;

	private String accession;
	private GenomicComponentMetaData component;
	
	/**
	 * @return the accession
	 */
	public String getAccession() {
		return accession;
	}

	/**
	 * @param accession the accession to set
	 */
	public void setAccession(String accession) {
		this.accession = accession;
	}

	public GenomicComponentMetaData getComponent() {
		return component;
	}

	public void setComponent(GenomicComponentMetaData component) {
		this.component = component;
	}

	public EnaParsingException() {
		super();
	}

	public EnaParsingException(String message, Throwable cause, String accession) {
		super(message, cause);
		this.accession = accession;
	}
	
	public EnaParsingException(String message, Throwable cause, GenomicComponentMetaData component) {
		super(message, cause);
		this.component = component;
	}


	public EnaParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnaParsingException(String message) {
		super(message);
	}
	
	public EnaParsingException(Throwable cause) {
		super(cause);
	}
	


}
