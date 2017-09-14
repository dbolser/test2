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
 * File: SequenceComponentIdentifier.java
 * Created by: dstaines
 * Created on: Feb 6, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.ac.ebi.proteome.resolver.EntityMetaData;
import uk.ac.ebi.proteome.resolver.IdentificationException;
import uk.ac.ebi.proteome.services.version.Section;
import uk.ac.ebi.proteome.services.version.VersionServiceException;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Base class encapsulation of identification of single sectioned metadata from
 * one or more sources
 *
 * @author dstaines
 *
 */
public abstract class AbstractSingleSectionedIdentifier<T extends EntityMetaData>
		extends AbstractIdentifier<T> {

	public AbstractSingleSectionedIdentifier(String srcName) {
		super(srcName);
	}

	public AbstractSingleSectionedIdentifier(Properties properties) {
		super(properties);
	}

	@Override
	protected Collection<T> getMetaData(IdentifierCriteria criteria)
			throws IdentificationException {
		Set<T> metaDataSet = new HashSet<T>();
		T metaData = null;

		if (criteria == IdentifierCriteria.DELETED) {

			// This URI might not be appropriate here e.g. web services etc.
			// TODO version service implementation might be asked for its URI!

			for (String source : dataSources) {
				try {
					// in this instance, a component is assumed to be a section
					// as
					// it is a single sequence
					for (Section section : getVersionService()
							.getDeletedSections(source)) {
						metaData = getMetaDataForSection(section);
						if (metaData != null) {
							metaData.setDeleted(true);
							metaData.setUpdated(false);
							metaDataSet.add(metaData);
						}
					}
				} catch (VersionServiceException e) {
					String msg = "Could not identify deleted components for source "
							+ source;
					getLog().error(msg, e);
					throw new IdentificationException(msg, e);
				}
			}
		} else {

			// This URI might not be appropriate here e.g. web services etc.
			// TODO version service implementation might be asked for its URI!
			for (String source : dataSources) {
				try {
					// in this instance, a component is assumed to be a section
					// as
					// it is a single sequence

					Set<Section> allSections = getVersionService().getSections(
							source);
					Set<Section> updSections = getVersionService()
							.getUpdatedSections(source);
					Set<Section> deletedSections = getVersionService()
							.getDeletedSections(source);

					for (Section section : allSections) {
						if (criteria == IdentifierCriteria.NEW) {
							if (updSections.contains(section)) {
								metaData = getMetaDataForSection(section);
								if (metaData != null) {
									metaData.setUpdated(true);
									metaData.setDeleted(false);
									metaDataSet.add(metaData);
								}
							}
						} else if (criteria == IdentifierCriteria.ALL) {
							// if criteria is all, we always add the metadata
							metaData = getMetaDataForSection(section);
							if (metaData != null) {
								metaData.setUpdated(updSections
										.contains(section));
								metaData.setDeleted(deletedSections
										.contains(section));
								metaDataSet.add(metaData);
							}
						} else {
							// no other criteria are supported for the moment
							String msg = "Criteria '"
									+ criteria
									+ "' is not supported for identifying metadata";
							getLog().error(msg);
							throw new IdentificationException(msg);
						}
					}
				} catch (VersionServiceException e) {
					String msg = "Could not identify metadata matching criteria '"
							+ criteria + "' for source " + source;
					getLog().error(msg, e);
					throw new IdentificationException(msg, e);
				}
			}
		}
		return metaDataSet;
	}

	Map<String, Section> sectionMap = null;

	private Map<String, Section> getSectionMap() throws VersionServiceException {
		if (sectionMap == null) {
			sectionMap = CollectionUtils.createHashMap();
			for (String source : dataSources) {
				for (Section section : getVersionService().getSections(source)) {
					sectionMap.put(section.getSection(), section);
				}
			}
		}
		return sectionMap;
	}

	@Override
	public T getMetaDataForIdentifier(String identifier, Object... params)
			throws IdentificationException {
		T metaData = null;
		try {
			// in this instance, a component is assumed to be a section as
			Section section = getSectionMap().get(identifier);
			if (section != null) {
				metaData = getMetaDataForSection(section);
				metaData.setUpdated(isMetaDataUpdated(metaData, params));
				metaData.setDeleted(isMetaDataDeleted(metaData, params));
			}
		} catch (VersionServiceException e) {
			String msg = "Could not identify component with identifier "
					+ identifier;
			getLog().error(msg, e);
			throw new IdentificationException(msg, e);
		}
		if (metaData == null) {
			throw new IdentificationException(
					"Could not identify entity with identifier " + identifier);
		}
		return metaData;
	}

	/**
	 * @param section
	 * @return
	 */
	protected abstract T getMetaDataForSection(Section section);

}
