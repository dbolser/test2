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
 * File: ComponentIdentifier.java
 * Created by: dstaines
 * Created on: Feb 6, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

import java.util.Collection;

/**
 * Interface specifying methods for determining which meta data are up-to-date
 * 
 * @author dstaines
 */
public interface IdentifierModule<T extends EntityMetaData> {

	/**
	 * Retrieve a metadata object for a given identifier
	 * 
	 * @param identifier
	 * @return metadata object
	 */
	public T getMetaDataForIdentifier(String identifier, Object... params)
			throws IdentificationException;

	/**
	 * Return a collection of new meta data
	 * 
	 * @return metadata collection
	 * @throws IdentificationException
	 */
	public Collection<T> getAllMetaData(Object... params) throws IdentificationException;

	/**
	 * Return a collection of metadata for updated items
	 * 
	 * @return updated metadata collection
	 * @throws IdentificationException
	 */
	public Collection<T> getUpdatedMetaData(Object... params) throws IdentificationException;

	/**
	 * Return a collection of metadata for deleted items
	 * 
	 * @return
	 * @throws IdentificationException
	 */
	public Collection<T> getDeletedMetaData(Object... params) throws IdentificationException;

	/**
	 * Find out if data from a particular meta data has been updated since last
	 * registered as seen
	 * 
	 * @param component
	 * @return true if data has been updated
	 * @throws IdentificationException
	 */
	public boolean isMetaDataUpdated(T component, Object... params)
			throws IdentificationException;

	/**
	 * Find out if data from particular meta data has been deleted since last
	 * registered as seen
	 * 
	 * @param component
	 * @return true if data has been deleted
	 * @throws IdentificationException
	 */
	public boolean isMetaDataDeleted(T component, Object... params)
			throws IdentificationException;

	/**
	 * Mark data from this particular meta data as seen
	 * 
	 * @param metadata
	 * @throws IdentificationException
	 */
	public void setMetaDataAsSeen(T metadata, Object... params) throws IdentificationException;

	public Collection<String> getMetaDataIdentifiers();

}
