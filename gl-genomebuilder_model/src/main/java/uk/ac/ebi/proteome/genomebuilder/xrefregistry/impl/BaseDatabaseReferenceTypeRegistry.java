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
 * File: BaseDatabaseReferenceTypeRegistry.java
 * Created by: dstaines
 * Created on: Oct 25, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl;

import static uk.ac.ebi.proteome.util.collections.CollectionUtils.createHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.collections.MultiListValueMap;

/**
 * @author dstaines
 * 
 */
public abstract class BaseDatabaseReferenceTypeRegistry implements
		DatabaseReferenceTypeRegistry {

	public BaseDatabaseReferenceTypeRegistry() {
		super();
	}

	public abstract void registerType(DatabaseReferenceType type);

	protected abstract MultiListValueMap<String, DatabaseReferenceType> getTypes();

	public DatabaseReferenceType getTypeForQualifiedName(String databaseName,
			String qualifier) {
		DatabaseReferenceType type = null;
		Collection<DatabaseReferenceType> typesForName = getTypesForName(databaseName);
		if (typesForName != null) {
			for (DatabaseReferenceType t : typesForName) {
				if (qualifier.equals(t.getQualifier())) {
					type = t;
					break;
				}
			}
		}
		return type;
	}

	public Collection<DatabaseReferenceType> getTypesForName(String databaseName) {
		return getTypes().getValues(databaseName);
	}

	public DatabaseReferenceType getTypeForName(String databaseName) {
		DatabaseReferenceType type = null;
		Collection<DatabaseReferenceType> typesForName = getTypesForName(databaseName);
		if (typesForName != null) {
			for (DatabaseReferenceType t : typesForName) {
				if (StringUtils.isEmpty(t.getQualifier())) {
					type = t;
					break;
				}
			}
		}
		return type;
	}

	public Collection<DatabaseReferenceType> getAllTypes() {
		List<DatabaseReferenceType> types = CollectionUtils.createArrayList();
		for (List<DatabaseReferenceType> typeList : getTypes().values()) {
			types.addAll(typeList);
		}
		return types;
	}

	private Map<Integer, DatabaseReferenceType> idLookup;

	/**
	 * Initalize on demand id lookup map. Uses the values from
	 * {@link #getAllTypes()} and loops through populating idLookup accordingly.
	 */
	protected Map<Integer, DatabaseReferenceType> getIdLookupMap() {
		if (idLookup == null) {
			Collection<DatabaseReferenceType> refs = getAllTypes();
			idLookup = createHashMap(refs.size() * 2);
			for (DatabaseReferenceType type : refs) {
				idLookup.put(type.getId(), type);
			}
		}
		return idLookup;
	}

	private Map<String, DatabaseReferenceType> nameLookup;

	/**
	 * Initalize on demand name lookup map. Uses values from
	 * {@link #getAllTypes()} and loops through populating nameLookup
	 * accordingly using all names, lowercased.
	 */
	protected Map<String, DatabaseReferenceType> getNameLookupMap() {
		if (nameLookup == null) {
			Collection<DatabaseReferenceType> refs = getAllTypes();
			nameLookup = createHashMap(refs.size() * 2);
			for (DatabaseReferenceType type : refs) {
				if(type.getDbName()==null) {
					System.out.println(type.getId());
				}
				nameLookup.put(type.getDbName().toLowerCase(), type);
				nameLookup.put(type.getDisplayName().toLowerCase(), type);
				nameLookup.put(type.getEnsemblName().toLowerCase(), type);
				nameLookup.put(type.getUniprotKbName().toLowerCase(), type);
			}
		}
		return nameLookup;
	}

	/**
	 * Uses the ID Lookup map backing this class to find a given identifier's
	 * {@link DatabaseReferenceType}
	 */
	public DatabaseReferenceType getType(int id) {
		return getIdLookupMap().get(id);
	}

	public DatabaseReferenceType getTypeForOtherName(String databaseName) {
		return getNameLookupMap().get(databaseName.toLowerCase());
	}
}
