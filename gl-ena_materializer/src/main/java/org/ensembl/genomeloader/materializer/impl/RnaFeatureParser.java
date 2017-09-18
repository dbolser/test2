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

import nu.xom.Element;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.genomebuilder.model.AnnotatedGene;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReference;
import org.ensembl.genomeloader.genomebuilder.model.GeneName;
import org.ensembl.genomeloader.genomebuilder.model.GeneNameType;
import org.ensembl.genomeloader.genomebuilder.model.impl.GeneImpl;
import org.ensembl.genomeloader.genomebuilder.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.genomebuilder.model.impl.RnaTranscriptImpl;
import org.ensembl.genomeloader.genomebuilder.model.impl.RnageneImpl;
import org.ensembl.genomeloader.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.materializer.EnaParsingException;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * @author dstaines
 * 
 */
public class RnaFeatureParser extends XmlEnaFeatureParser {

	public static String ANALYSIS = "ENA_RNA";

	public RnaFeatureParser(DatabaseReferenceTypeRegistry registry) {
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
		RnageneImpl gene = new RnageneImpl();
		DatabaseReference ref = getFeatureIdentifierRef(component, element, "GENE");
		gene.addDatabaseReference(ref);
		gene.setAnalysis(ANALYSIS);
		String featureName = element.getAttributeValue("name");
		String productName = CollectionUtils.getFirstElement(qualifiers.get("product"),null);
		if (!StringUtils.isEmpty(productName)) {
			gene.setDescription(productName);
		}
		gene.setBiotype(featureName);
		AnnotatedGene geneName = getGeneName(qualifiers);
		if (geneName.getNameCount() > 0) {
			gene.addAnnotatedGene(geneName);
			gene.setName(geneName.getIdentifyingId());
		} else if (!StringUtils.isEmpty(productName)) {
			// use the product if we can
			gene.setName(productName);
		} else {
			// fall back on the less than useful feature name
			gene.setName(featureName);
		}
		gene.setIdentifyingId(getGeneName(gene));
		if(StringUtils.isEmpty(gene.getIdentifyingId())) {
			getLog().warn("Could not find locus tag for gene "+featureName);
		}
		gene.setLocation(parseLocation(element));
		for (DatabaseReference xref : parseXrefs(element)) {
			gene.addDatabaseReference(xref);
		}
		gene.setPseudogene(qualifiers.containsKey("pseudo"));
		
		final RnaTranscriptImpl t = new RnaTranscriptImpl();
		t.setName(gene.getName());
		t.setDescription(gene.getDescription());
		t.setLocation(gene.getLocation());
		t.setBiotype(gene.getBiotype());
		t.setAnalysis(gene.getAnalysis());
		t.setPseudogene(gene.isPseudogene());
		gene.addTranscript(t);
		component.getRnagenes().add(gene);
	}
	
	private String getGeneName(RnageneImpl gene) {
		List<GeneName> names = gene.getNameMap().get(
				GeneNameType.ORDEREDLOCUSNAMES);
		if (names != null && names.size() > 0) {
			return names.get(0).getName();
		} else {
			return null;
		}
	}

}
