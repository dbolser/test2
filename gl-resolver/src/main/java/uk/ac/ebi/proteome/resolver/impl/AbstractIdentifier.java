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
 * File: AbstractIdentifier.java
 * Created by: dstaines
 * Created on: Jul 11, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.resolver.DataItem;
import uk.ac.ebi.proteome.resolver.EntityMetaData;
import uk.ac.ebi.proteome.resolver.IdentificationException;
import uk.ac.ebi.proteome.resolver.IdentifierModule;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.version.Section;
import uk.ac.ebi.proteome.services.version.VersionService;
import uk.ac.ebi.proteome.services.version.VersionServiceException;

/**
 * Base implementation of an identifier that interacts with one or more sources
 * in the version service. The relationship between sources, sections and
 * metadata is not specified in this base class.
 *
 * @author dstaines
 *
 * @param <T>
 */
public abstract class AbstractIdentifier<T extends EntityMetaData> implements
		IdentifierModule<T> {

	protected enum IdentifierCriteria {
		ALL, NEW, DELETED;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.resolver.IdentifierModule#getMetaDataForIdentifier(java.lang.String,
	 *      java.lang.Object[])
	 */
	public abstract T getMetaDataForIdentifier(String identifier,
			Object... params) throws IdentificationException;

	/**
	 * Method that returns a set of metadata matching the given criteria
	 *
	 * @param criteria
	 *            scope of the query
	 * @return collection of relevant metadata
	 * @throws IdentificationException
	 */
	protected abstract Collection<T> getMetaData(IdentifierCriteria criteria)
			throws IdentificationException;

	/**
	 * Construction property specifying list of sources separated by commas
	 */
	public static final String SOURCES_PROPERTY = "sources";
	protected List<String> dataSources = new ArrayList<String>();
	protected Log log;
	protected VersionService srv = null;

	public AbstractIdentifier(String srcName) {
		dataSources.add(srcName);
	}

	public AbstractIdentifier(Properties properties) {
		if (properties.containsKey(SOURCES_PROPERTY)) {
			String sources = properties.getProperty(SOURCES_PROPERTY);
			if (!StringUtils.isEmpty(sources)) {
				dataSources = Arrays.asList(sources.split(" *, *"));
			}
		}
	}

	/**
	 * Get the list of data sources that are used for component identification
	 *
	 * @return
	 */
	/**
	 * @return
	 */
	public List<String> getDataSources() {
		if (dataSources == null) {
			dataSources = new ArrayList<String>();
		}
		return this.dataSources;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.resolver.IdentifierModule#isMetaDataUpdated(uk.ac.ebi.proteome.resolver.EntityMetaData,
	 *      java.lang.Object[])
	 */
	public boolean isMetaDataUpdated(T component, Object... params)
			throws IdentificationException {
		// TODO may need someway to check URI and warn/barf if not matching
		try {
			boolean isUpdated = false;
			for (DataItem item : component.getDataItems()) {
				if (getVersionService().isSectionUpdated(
						item.getSection().getDatasource(),
						item.getSection().getSection())) {
					isUpdated = true;
					item.setUpdated(true);
				}
			}
			return isUpdated;
		} catch (VersionServiceException e) {
			String msg = "Could not check status of component '"
					+ component.getIdentifier() + "' of type "
					+ component.getDataType();
			getLog().error(msg, e);
			throw new IdentificationException(msg, e);
		}
	}

	/**
	 * Set the list of data sources used for component identification
	 *
	 * @param dataSources
	 */
	/**
	 * @param dataSources
	 */
	public void setDataSources(List<String> dataSources) {
		this.dataSources = dataSources;
	}

	/**
	 * @return
	 */
	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	/**
	 * @return
	 */
	protected VersionService getVersionService() {
		if (srv == null) {
			srv = ServiceContext.getInstance().getVersionService();
		}
		return srv;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.resolver.IdentifierModule#setMetaDataAsSeen(uk.ac.ebi.proteome.resolver.EntityMetaData,
	 *      java.lang.Object[])
	 */
	public void setMetaDataAsSeen(T component, Object... params)
			throws IdentificationException {
		try {
			for (DataItem item : component.getDataItems()) {
				Section section = item.getSection();
				if (section == null) {
					log
							.warn("Supplied data item "
									+ item.getSource().getName()
									+ ":"
									+ item.getIdentifier()
									+ " unknown to VersionService - registering new section");
					section = getVersionService().getSection(
							item.getSource().getName(), item.getIdentifier());
					item.setSection(section);
				}
				getVersionService().registerSectionAsRead(section);
			}
		} catch (VersionServiceException e) {
			String msg = "Could not register 'seen' status of component '"
					+ component.getIdentifier() + "' for source "
					+ component.getDataType();
			getLog().error(msg, e);
			throw new IdentificationException(msg, e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.resolver.IdentifierModule#getDeletedMetaData(java.lang.Object[])
	 */
	public Collection<T> getDeletedMetaData(Object... params)
			throws IdentificationException {
		return getMetaData(IdentifierCriteria.DELETED);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.resolver.IdentifierModule#getAllMetaData(java.lang.Object[])
	 */
	public Collection<T> getAllMetaData(Object... params)
			throws IdentificationException {
		return getMetaData(IdentifierCriteria.ALL);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.resolver.IdentifierModule#getUpdatedMetaData(java.lang.Object[])
	 */
	public Collection<T> getUpdatedMetaData(Object... params)
			throws IdentificationException {
		return getMetaData(IdentifierCriteria.NEW);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.resolver.IdentifierModule#isMetaDataDeleted(uk.ac.ebi.proteome.resolver.EntityMetaData,
	 *      java.lang.Object[])
	 */
	public boolean isMetaDataDeleted(T component, Object... params)
			throws IdentificationException {
		try {
			boolean deleted = false;
			Section currS = null;
			for (DataItem item : component.getDataItems()) {
				Section sec = item.getSection();
				// TODO not sure on behaviour of getSection for deleted sections
				currS = getVersionService().getSection(sec.getDatasource(),
						sec.getSection());
				if (!currS.isAlive()) {
					deleted = true;
					item.setDeleted(true);
				}
			}
			return deleted;
		} catch (VersionServiceException e) {
			String msg = "Could not check status of entity '"
					+ component.getIdentifier() + "' of type "
					+ component.getDataType();
			getLog().error(msg, e);
			throw new IdentificationException(msg, e);
		}
	}

}
