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
 * File: SourceDefinition.java
 * Created by: dstaines
 * Created on: Jan 25, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.metadata;

import java.io.Serializable;

/**
 * Bean containing minimal metadata describing a datasource
 * 
 * @author dstaines
 */
public class SourceDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name = "";

	private String type = "";

	private String uri = "";

	public SourceDefinition() {
	}

	public SourceDefinition(String name, String uri, String type) {
		this.name = name;
		this.uri = uri;
		this.type = type;
	}

	/**
	 * Return a meaningful, unique name for the source
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return a string representing the particular type of data
	 * 
	 * @return
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Return a string representing how to obtain this data source
	 * 
	 * @return
	 */
	public String getUri() {
		return this.uri;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String toString() {
		StringBuilder s =new StringBuilder();
		s.append(name);
		s.append(" (");
		s.append(type);
		s.append(") ");
		s.append(uri);
		return s.toString();
	}
	
}
