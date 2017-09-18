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
 * File: LocationAlteration.java
 * Created by: dstaines
 * Created on: Dec 14, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model;

/**
 * Base class representing a modification to a genomic location
 * 
 * @author dstaines
 * 
 */
public abstract class EntityLocationModifier implements Integr8ModelComponent {

	private static final long serialVersionUID = 1L;
	private int start;
	private int stop;
	private final String proteinSeq;
	public static final String INSERTION_TYPE = "I";
	public static final String EXCEPTION_TYPE = "E";

	public EntityLocationModifier(int start, int stop, String proteinSeq) {
		super();
		this.start = start;
		this.stop = stop;
		this.proteinSeq = proteinSeq;
	}

	public int getStart() {
		return start;
	}

	public int getStop() {
		return stop;
	}

	public String getProteinSeq() {
		return proteinSeq;
	}
	
	public String getIdString() {
		return toString();
	}
	
	public void setStart(int i) {
		this.start = i;
	}
	public void setStop(int i) {
		this.stop = i;
	}

}
