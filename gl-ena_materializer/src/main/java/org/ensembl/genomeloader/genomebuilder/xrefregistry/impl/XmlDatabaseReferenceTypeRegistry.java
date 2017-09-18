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
 * File: XmlToDatabaseTypeReferences.java
 * Created by: dstaines
 * Created on: Oct 25, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.xrefregistry.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;

import org.ensembl.genomeloader.genomebuilder.model.DatabaseReferenceType;
import org.ensembl.genomeloader.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.services.ServiceUncheckedException;
import org.ensembl.genomeloader.util.InputOutputUtils;
import org.ensembl.genomeloader.util.UtilUncheckedException;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.collections.MultiListValueMap;

import com.thoughtworks.xstream.XStream;

/**
 * Registry implementation that reads {@link DatabaseReferenceType} instances
 * from a specified or reference xml file
 *
 * @author dstaines
 *
 */
public class XmlDatabaseReferenceTypeRegistry extends
		BaseDatabaseReferenceTypeRegistry implements
		DatabaseReferenceTypeRegistry {

	File file = null;
	MultiListValueMap<String, DatabaseReferenceType> typeMap;

	public XmlDatabaseReferenceTypeRegistry() {
	}

	public XmlDatabaseReferenceTypeRegistry(File file) {
		this.file = file;
	}

	protected Reader getReader() throws ServiceUncheckedException {
		Reader reader = null;
		if (file == null) {
			String cfgFile = "/uk/ac/ebi/proteome/genomebuilder/xrefregistry/impl/xrefreg.xml";
			try {
				reader = InputOutputUtils
						.slurpTextClasspathResourceToStringReader(cfgFile);
			} catch (UtilUncheckedException e) {
				throw new ServiceUncheckedException(
						"Could not create reader from " + cfgFile, e);
			}
		} else {
			try {
				reader = new FileReader(file);
			} catch (FileNotFoundException e) {
				throw new ServiceUncheckedException(
						"Could not create reader from " + file, e);
			}
		}
		return reader;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.impl.DatabaseReferenceTypeRegistry#registerType(org.ensembl.genomeloader.genomebuilder.model.DatabaseReferenceType)
	 */
	@Override
	public void registerType(DatabaseReferenceType type) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.impl.BaseDatabaseReferenceTypeRegistry#getTypes()
	 */
	@Override
	protected MultiListValueMap<String, DatabaseReferenceType> getTypes() {
		if (typeMap == null) {
			typeMap = CollectionUtils.createMultiListValueMap();
			for (DatabaseReferenceType type : ((DatabaseReferenceTypes) getXstream()
					.fromXML(getReader())).getTypes()) {
				typeMap.put(type.getDbName(), type);
			}
		}
		return typeMap;
	}

	XStream xstream;

	protected XStream getXstream() {
		if (xstream == null) {
			xstream = new XStream();
			xstream.alias("databaseReferenceType", DatabaseReferenceType.class);
			xstream.alias("databaseReferenceTypes",
					DatabaseReferenceTypes.class);
			xstream
					.addImplicitCollection(DatabaseReferenceTypes.class,
							"types");
		}
		return xstream;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.impl.DatabaseReferenceTypeRegistry#deleteType(org.ensembl.genomeloader.genomebuilder.model.DatabaseReferenceType)
	 */
	public void deleteType(DatabaseReferenceType type) {
		throw new UnsupportedOperationException();
	}

	public void databaseReferenceTypesToXml(Writer writer,
			Collection<DatabaseReferenceType> typesToWrite) {
		DatabaseReferenceTypes tObj = new DatabaseReferenceTypes(typesToWrite);
		getXstream().toXML(tObj, writer);
	}

	public static final void main(String[] args) throws IOException {
		XmlDatabaseReferenceTypeRegistry reg = new XmlDatabaseReferenceTypeRegistry();
		Collection<DatabaseReferenceType> types = reg
				.getTypesForName("UNIPROT");
		reg.databaseReferenceTypesToXml(new FileWriter("/tmp/test.xml"), types);
	}

}
