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
 * File: DatabaseReferenceTypeRegistry.java
 * Created by: dstaines
 * Created on: Oct 25, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.xrefregistry;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;

import java.util.Collection;

/**
 * Interface for retrieving instances of {@link DatabaseReferenceType}
 *
 * @author dstaines
 */
public interface DatabaseReferenceTypeRegistry {

	/**
	 * Get all {@link DatabaseReferenceType} instances from the registry
	 *
	 * @return collection of all types
	 */
	public Collection<DatabaseReferenceType> getAllTypes();

	/**
	 * Used for direct access of database reference types
	 *
	 * @param id The id of the type
	 * @return An instance of the database type. Will return null if one
	 * cannot be found
	 */
	public DatabaseReferenceType getType(int id);

	/**
	 * Get all {@link DatabaseReferenceType} instances that share the same
	 * database name
	 *
	 * @param databaseName
	 *            name to use
	 * @return collection of matching types
	 */
	public Collection<DatabaseReferenceType> getTypesForName(String databaseName);

	/**
	 * Get instance of {@link DatabaseReferenceType} with the specified database
	 * name and qualifier
	 *
	 * @param databaseName
	 *            main database name
	 * @param qualifier
	 *            qualifier
	 * @return matching type or null if none found
	 */
	public DatabaseReferenceType getTypeForQualifiedName(String databaseName,
			String qualifier);

	/**
	 * Get instance of {@link DatabaseReferenceType} with the specified database
	 * name but with no qualifier
	 *
	 * @param databaseName
	 *            main database name
	 * @return matching type or null if none found
	 */
	public DatabaseReferenceType getTypeForName(String databaseName);

	/**
	 * Write the specified instance from the registry
	 *
	 * @param type
	 *            instance to register
	 */
	public void registerType(DatabaseReferenceType type);

	/**
	 * Delete the specified type from the registry
	 *
	 * @param type
	 *            instance to delete
	 */
	public void deleteType(DatabaseReferenceType type);
	
	public DatabaseReferenceType getTypeForOtherName(String databaseName);

}
