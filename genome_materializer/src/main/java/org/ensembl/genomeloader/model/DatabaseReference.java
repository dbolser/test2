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
 * File: DatabaseReference.java
 * Created by: dstaines
 * Created on: Apr 13, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model;

/**
 * Base interface for describing a cross-reference to an external database
 * 
 * @author dstaines
 * 
 */
public interface DatabaseReference extends Integr8ModelComponent {

	public DatabaseReferenceType getDatabaseReferenceType();

	/**
	 * @return mandatory primary identifier
	 */
	public String getPrimaryIdentifier();

	/**
	 * @return optional identifier
	 */
	public String getSecondaryIdentifier();

	/**
	 * @return optional identifier
	 */
	public String getTertiaryIdentifier();

	/**
	 * @return optional identifier
	 */
	public String getQuarternaryIdentifier();

	/**
	 * @return optional description of the reference
	 */
	public String getDescription();

	/**
	 * @return source database from which this crossreference was derived
	 */
	public DatabaseReference getSource();

	/**
	 * @return measure of identity for this xref
	 */
	public Double getIdentity();

	/**
	 * @param identity
	 *            measure of identity for this xref
	 */
	public void setIdentity(Double identity);

	/**
	 * @return true if this xref refers to an identical entity
	 */
	public boolean isIdentityXref();

	/**
	 * @return optional version of entry
	 */
	public String getVersion();

	/**
	 * @param version
	 *            optional version of entry
	 */
	public void setVersion(String version);

	public void setSource(DatabaseReference source);

}
