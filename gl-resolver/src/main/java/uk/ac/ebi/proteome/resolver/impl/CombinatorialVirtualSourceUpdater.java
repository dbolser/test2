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
 * File: VirtualSourceUpdater.java
 * Created by: dstaines
 * Created on: Jul 5, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.resolver.IdentificationUncheckedException;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.version.Section;
import uk.ac.ebi.proteome.services.version.VersionService;
import uk.ac.ebi.proteome.services.version.VersionServiceException;

/**
 * Utility class that encapsulates interactions with version service to create
 * and/or update a virtual source from a combination of each of a list section from one
 * or more sectional sources with all sections from one or more unsectional sources
 *
 * @author dstaines
 *
 */
public class CombinatorialVirtualSourceUpdater implements VirtualSourceUpdater {

	protected Log log;
	protected List<String> sectionedSrcs = null;
	protected VersionService srv = null;
	protected List<String> unsectionedSrcs = null;
	protected String virtualSrc = null;

	public CombinatorialVirtualSourceUpdater(String virtualSrc,
			List<String> sectionedSrcs, List<String> unsectionedSrcs) {
		this.virtualSrc = virtualSrc;
		this.sectionedSrcs = sectionedSrcs;
		this.unsectionedSrcs = unsectionedSrcs;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.mirror.sequence.impl.VirtualSourceUpdater#update()
	 */
	public void update() {

		// utility to assemble a list of "virtual" sections which are composed
		// of:
		// 1. a single section from one of the sectioned sources
		// 2. all sections from the unsectioned sources

		// 1. assemble list of updated unsectioned sections
		Set<Section> unsectionedSections = new HashSet<Section>();
		boolean unsectionedUpd = assembleUnsectionedSections(unsectionedSections);
		getLog().debug(
				"Assembled list of " + unsectionedSections.size()
						+ " unsectioned sections which were "
						+ (unsectionedUpd ? "" : "not ") + "updated");

		// 2. get list of sectioned sections (force if unsectioned updated)
		// 3. get list of deleted sections
		Set<Section> updatedSections = new HashSet<Section>();
		Set<Section> deletedSections = new HashSet<Section>();
		assembleSectionedSections(unsectionedUpd, updatedSections,
				deletedSections);
		getLog().debug(
				"Assembled list of " + updatedSections.size() + " updated and "
						+ deletedSections.size()
						+ " deleted component sections");

		// 4. for each updated sectioned section
		// get a section from the virtual section representing this
		// set the external version string based on the versions of the input
		// sections
		// set section as written
		int n = updateVirtualSections(unsectionedSections, updatedSections);
		getLog().debug("Updated " + n + " virtual sections");

		// 5. for each deleted sectioned section, delete the corresponding
		// unsectioned section
		n = deleteVirtualSections(deletedSections);
		getLog().debug("Deleted " + n + " virtual sections");

		// 6. set everything as read
		setSectionsAsRead(unsectionedSections, updatedSections, deletedSections);

	}

	/**
	 * Method to generate lists of sectioned components
	 *
	 * @param unsectionedUpd
	 *            true if the unsectioned sections have been updated already
	 * @param updatedSections
	 *            list to put updated sections into
	 * @param deletedSections
	 *            list to put deleted sections into
	 */
	protected void assembleSectionedSections(boolean unsectionedUpd,
			Set<Section> updatedSections, Set<Section> deletedSections) {
		for (String src : sectionedSrcs) {
			try {
				if (unsectionedUpd) {
					updatedSections
							.addAll(getVersionService().getSections(src));
				} else {
					updatedSections.addAll(getVersionService()
							.getUpdatedSections(src));
				}
				deletedSections.addAll(getVersionService().getDeletedSections(
						src));
			} catch (VersionServiceException e) {
				throw new IdentificationUncheckedException(
						"Could not check sectioned components of " + src
								+ " for virtual source " + virtualSrc, e);
			}
		}
	}

	/**
	 * Method to assemble set of sections from "unsectioned" datasources that
	 * apply to all sectional sources
	 *
	 * @param unsectionedSections
	 *            set to populate with sections
	 * @return true if any section has been updated
	 */
	protected boolean assembleUnsectionedSections(
			Set<Section> unsectionedSections) {
		boolean unsectionedUpd = false;
		try {
			for (String src : unsectionedSrcs) {
				if (getVersionService().isDataSourceUpdated(src)) {
					unsectionedUpd = true;
				}
				unsectionedSections
						.addAll(getVersionService().getSections(src));
			}
		} catch (VersionServiceException e) {
			throw new IdentificationUncheckedException(
					"Could not check component sections for virtual source "
							+ virtualSrc, e);
		}
		return unsectionedUpd;
	}

	/**
	 * Method to set virtual sections as deleted from set of component sections
	 *
	 * @param deletedSections
	 *            set of input component sections that have been deleted
	 */
	protected int deleteVirtualSections(Set<Section> deletedSections) {
		int n = 0;
		for (Section section : deletedSections) {
			n++;
			try {
				Section vSection = getVersionService().getSection(virtualSrc,
						section.getSection());
				getVersionService().registerSectionAsDeleted(vSection);
			} catch (VersionServiceException e) {
				throw new IdentificationUncheckedException(
						"Could not set section " + section
								+ " as read for virtual source " + virtualSrc,
						e);
			}
		}
		return n;
	}

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	protected VersionService getVersionService() {
		if (srv == null) {
			srv = ServiceContext.getInstance().getVersionService();
		}
		return srv;
	}

	/**
	 * Set all the supplied sections as "read"
	 *
	 * @param unsectionedSections
	 *            underlying unsectioned sections
	 * @param updatedSections
	 *            updated sectional components
	 * @param deletedSections
	 *            deleted sectional components
	 */
	protected void setSectionsAsRead(Set<Section> unsectionedSections,
			Set<Section> updatedSections, Set<Section> deletedSections) {
		try {
			getVersionService().registerDataSourceAsRead(unsectionedSections);
			getVersionService().registerDataSourceAsRead(updatedSections);
			getVersionService().registerDataSourceAsRead(deletedSections);
		} catch (VersionServiceException e) {
			throw new IdentificationUncheckedException(
					"Could not set component sections as read for virtual source "
							+ virtualSrc, e);
		}
	}

	/**
	 * Method to process the updated sections and update them if necessary
	 *
	 * @param unsectionedSections
	 *            list of all sections from unsectioned sources
	 * @param updatedSections
	 *            List of updated sections from the sectional sources
	 */
	protected int updateVirtualSections(Set<Section> unsectionedSections,
			Set<Section> updatedSections) {
		try {
			int n = 0;
			StringBuilder unsectionedVersionString = new StringBuilder();
			boolean comma = false;
			for (Section section : unsectionedSections) {
				if (comma)
					unsectionedVersionString.append(',');
				comma = true;
				unsectionedVersionString.append(section.getDatasource());
				unsectionedVersionString.append('[');
				unsectionedVersionString.append(section.getExternalVersion());
				unsectionedVersionString.append(']');
			}
			for (Section section : updatedSections) {
				n++;
				Section vSection = getVersionService().getSection(virtualSrc,
						section.getDatasource() + ':' + section.getSection());
				// create a version string
				StringBuilder extVer = new StringBuilder();
				extVer.append(section.getDatasource());
				extVer.append('[');
				extVer.append(section.getSection());
				extVer.append(':');
				extVer.append(section.getExternalVersion());
				extVer.append(']');
				extVer.append(',');
				extVer.append(unsectionedVersionString);
				vSection.setExternalVersion(extVer.toString());
				vSection.setRowCount(section.getRowCount());
				// tell the version service that we wrote the
				getVersionService().registerSectionAsWritten(vSection);
			}
			return n;
		} catch (VersionServiceException e) {
			throw new IdentificationUncheckedException(
					"Could not create component sections for virtual source "
							+ virtualSrc, e);
		}
	}
}
