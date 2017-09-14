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
 * File: VersionUtils.java
 * Created by: dstaines
 * Created on: Oct 6, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer.version;

import java.util.Collection;
import java.util.Date;

import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.resolver.DataItem;
import uk.ac.ebi.proteome.resolver.MaterializationUncheckedException;
import uk.ac.ebi.proteome.resolver.SourceDefinition;
import uk.ac.ebi.proteome.services.version.Section;
import uk.ac.ebi.proteome.services.version.VersionService;
import uk.ac.ebi.proteome.services.version.VersionServiceException;
import uk.ac.ebi.proteome.services.version.impl.VersionServiceImpl;

/**
 * Utilities for working with versioning
 *
 * @author dstaines
 *
 */
public class VersionUtils {

	private final static SourceDefinition genomicComponentSrc = new SourceDefinition(
			GenomicComponent.GENOMIC_COMPONENT, null, GenomicComponent.GENOMIC_COMPONENT);
	private final static SourceDefinition genomeSrc = new SourceDefinition(
			GenomeInfo.GENOME, null, GenomeInfo.GENOME);

	public static final SourceDefinition getGenomicComponentSrc() {
		return genomicComponentSrc;
	}

	public static final SourceDefinition getGenomeSrc() {
		return genomeSrc;
	}

	/**
	 * Decorate the supplied genomic component with version information
	 *
	 * @param srv
	 *            VersionService to use
	 * @param component
	 *            component to decorate
	 */
	public static final void versionComponent(VersionService srv,
			GenomicComponent component) {
		try {
			DataItem dataItem = new DataItem();
			dataItem.setSource(getGenomicComponentSrc());
			dataItem.setIdentifier(String.valueOf(component.getId()));
			dataItem.setSection(srv.getSection(dataItem.getSource().getName(),
					dataItem.getIdentifier()));
			component.getMetaData().getDataItems().add(dataItem);
		} catch (VersionServiceException e) {
			throw new MaterializationUncheckedException(
					"Could not decorate component " + component.getId()
							+ " with version information");
		} finally {
		}
	}

	private static void checkService(VersionService srv) {
		if (!VersionServiceImpl.class.isAssignableFrom(srv.getClass())) {
			throw new MaterializationUncheckedException(
					"Cannot version component using an instance of "
							+ srv.getClass().getSimpleName() + ": "
							+ VersionServiceImpl.class.getSimpleName()
							+ " compatible class required");
		}
	}

	/**
	 * Decorate the supplied genome with version information
	 *
	 * @param srv
	 *            VersionService to use
	 * @param genome
	 *            genome to decorate
	 */
	public static final void versionGenome(VersionService srv, Genome genome) {
		try {
			DataItem dataItem = new DataItem();
			dataItem.setSource(getGenomeSrc());
			dataItem.setIdentifier(String.valueOf(genome.getId()));
			dataItem.setSection(srv.getSection(dataItem.getSource().getName(),
					dataItem.getIdentifier()));
			genome.getDataItems().add(dataItem);
		} catch (VersionServiceException e) {
			throw new MaterializationUncheckedException(
					"Could not decorate genome " + genome.getId()
							+ " with version information");
		} finally {
		}
	}

	private static Section getSectionForSrc(SourceDefinition src,
			Collection<DataItem> items) {
		DataItem item = null;
		for (DataItem i : items) {
			if (i.getSource().getName() == src.getName()) {
				item = i;
				break;
			}
		}
		if (item == null)
			throw new MaterializationUncheckedException(
					"Could not find section for src " + src);
		return item.getSection();
	}

	public static final int getVersionForComponent(GenomicComponent component) {
		return getSectionForSrc(getGenomicComponentSrc(),
				component.getMetaData().getDataItems()).getDataVersion();
	}

	public static final int getVersionForGenome(Genome genome) {
		return getSectionForSrc(getGenomeSrc(), genome.getDataItems())
				.getDataVersion();
	}

	public static final Date getDateForGenome(VersionService srv, Genome genome) {
		try {
			checkService(srv);
			Section s = getSectionForSrc(getGenomeSrc(), genome.getDataItems());
			return ((VersionServiceImpl) srv).getDateForSection(s);
		} catch (VersionServiceException e) {
			throw new MaterializationUncheckedException(
					"Could not get date for genome " + genome.getId(), e);
		} finally {
		}
	}

	public static final Date getDateForComponent(VersionService srv,
			GenomicComponent component) {
		try {
			checkService(srv);
			Section s = getSectionForSrc(getGenomicComponentSrc(), component
					.getMetaData().getDataItems());
			return ((VersionServiceImpl) srv).getDateForSection(s);
		} catch (VersionServiceException e) {
			throw new MaterializationUncheckedException(
					"Could not get date for component " + component.getId(), e);
		} finally {
		}
	}

}
