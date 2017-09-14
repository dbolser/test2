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
 * File: SourceMetaData.java
 * Created by: dstaines
 * Created on: Mar 29, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import uk.ac.ebi.proteome.services.version.Section;

/**
 * Object describing a piece of data that contributes to an entity. Optionally
 * may contain a section, an identifier and a source definition
 *
 * @author dstaines
 *
 */
public class DataItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String identifier;

	private Section section = null;

	private SourceDefinition source;

	private boolean updated = false;

	private boolean deleted = false;

	private boolean sectioned = false;

	/**
	 * Optional identifier for describing an item from the source that is
	 * pertinent
	 *
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Section of data item
	 *
	 * @return
	 */
	public Section getSection() {
		return this.section;
	}

	/**
	 * @return
	 */
	public SourceDefinition getSource() {
		return source;
	}

	/**
	 * @return true if the data item has been updated
	 */
	public boolean isUpdated() {
		return this.updated;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @param section
	 *            the section to set
	 */
	public void setSection(Section section) {
		this.section = section;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(SourceDefinition source) {
		this.source = source;
	}

	/**
	 * @param updated
	 *            whether the data item has been updated
	 */
	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	/**
	 * @return true if data item has been deleted
	 */
	public boolean isDeleted() {
		return this.deleted;
	}

	/**
	 * @param deleted true if the data item has been deleted
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * @return true if the data source is sectioned
	 */
	public boolean isSectioned() {
		return this.sectioned;
	}

	/**
	 * @param sectioned true if the data source is sectioned
	 */
	public void setSectioned(boolean sectioned) {
		this.sectioned = sectioned;
	}

	public String toString() {
		return ReflectionToStringBuilder.reflectionToString(this);
	}

}
