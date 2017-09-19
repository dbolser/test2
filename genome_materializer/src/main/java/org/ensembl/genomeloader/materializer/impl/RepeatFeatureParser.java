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
 * File: CdsFeatureParser.java
 * Created by: dstaines
 * Created on: Mar 25, 2010
 * CVS:  $$
 */
package org.ensembl.genomeloader.materializer.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.model.impl.RepeatRegionImpl;
import org.ensembl.genomeloader.model.impl.RepeatRegionImpl.RepeatUnitImpl;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

import nu.xom.Element;

/**
 * @author dstaines
 * 
 */
public class RepeatFeatureParser extends XmlEnaFeatureParser {

	private final static String ENA_REPEAT = "ENA_REPEAT";

	public RepeatFeatureParser(DatabaseReferenceTypeRegistry registry) {
		super(registry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.materializer.ena.impl.XmlEnaFeatureParser#dependsOn()
	 */
	public List<Class<? extends XmlEnaFeatureParser>> dependsOn() {
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.materializer.ena.impl.XmlEnaFeatureParser#parseFeature
	 * (org.ensembl.genomeloader.genomebuilder.model.impl.GenomicComponentImpl,
	 * nu.xom.Element)
	 */
	public void parseFeature(GenomicComponentImpl component, Element element) {
		// 1. parse out qualifiers
		Map<String, List<String>> qualifiers = getQualifiers(element);
		// parse out location
		// create genes
		RepeatRegionImpl repeat = new RepeatRegionImpl();
		DatabaseReference ref = getFeatureIdentifierRef(component, element,
				"REPEAT");
		repeat.addDatabaseReference(ref);
		repeat.getDatabaseReferences().addAll(parseXrefs(element));
		String rptType = CollectionUtils.getFirstElement(
				qualifiers.get("rpt_type"), ENA_REPEAT);
		String rptFamily = CollectionUtils.getFirstElement(
				qualifiers.get("rpt_type"), rptType);
		List<String> notes = qualifiers.get("note");
		String rptName = null;
		if (notes == null || notes.size()==0) {
			rptName = rptFamily;
		} else {
			rptName = StringUtils.join(notes.iterator(), ";");
		}
		rptName = rptName.replaceAll("\\n", "");
		repeat.setAnalysis(rptType);
		RepeatUnitImpl ru = new RepeatRegionImpl.RepeatUnitImpl();
		ru.setRepeatType(rptType);
		ru.setRepeatClass(rptFamily);
		ru.setRepeatName(rptName);
		repeat.setRepeatUnit(ru);
		repeat.setLocation(parseLocation(element));
		component.getRepeats().add(repeat);
	}

}
