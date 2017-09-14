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
 * File: GenomicComponentMetaData.java
 * Created by: dstaines
 * Created on: Mar 9, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

/**
 * ResolverMetaData describing a genomic entity. Also contains some basic properties of the
 * component (scope, type, superregnum) to allow filtering.
 *
 * @author dstaines
 *
 */
public abstract class GenomicEntityMetaData extends EntityMetaData {

	private static final long serialVersionUID = 1L;

	public GenomicEntityMetaData() {
	}

	public GenomicEntityMetaData(String src, String id) {
		super(src, id);
	}

	private int type;

	private String superregnum = null;

	private Scope scope = Scope.CELLULAR;

	public Scope getScope() {
		return this.scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public String getSuperregnum() {
		return this.superregnum;
	}

	public void setSuperregnum(String superregnum) {
		this.superregnum = superregnum;
	}

	public int getType() {
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
