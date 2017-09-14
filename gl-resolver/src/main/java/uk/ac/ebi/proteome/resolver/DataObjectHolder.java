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
 * File: DataObject.java
 * Created by: dstaines
 * Created on: Jan 25, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

import java.io.Serializable;

/**
 * Simple bean wrapping for the data representing a component retrieved from a
 * materialiser
 * 
 * @author dstaines
 */
@Deprecated
public class DataObjectHolder<T> implements
		Serializable {

	private static final long serialVersionUID = 1L;

	private String comment = "";

	private String type = "";

	private T value;

	public String getComment() {
		return this.comment;
	}

	public String getType() {
		return this.type;
	}

	public T getValue() {
		return this.value;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setValue(T value) {
		this.value = value;
	}

}
