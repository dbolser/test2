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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ensembl.genomeloader.model.AnnotatedGene;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.EntityLocation;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.ModelUtils;
import org.ensembl.genomeloader.model.Pseudogene;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

import nu.xom.Element;

/**
 * @author dstaines
 * 
 */
public class LocationBasedGeneFeatureParser extends CdsFeatureParser {

	public LocationBasedGeneFeatureParser(DatabaseReferenceTypeRegistry registry) {
		super(registry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.materializer.ena.impl.XmlEnaFeatureParser#dependsOn()
	 */
	public List<Class<? extends XmlEnaFeatureParser>> dependsOn() {
		return new ArrayList<Class<? extends XmlEnaFeatureParser>>() {
			{
				add(CdsFeatureParser.class);
			}
		};
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
		// approach is to find genes that these genes enclose and then add
		// additional information
		// 1. xrefs
		// 2. names
		// 3. not sure what else?
		EntityLocation loc = parseLocation(element);
		Map<String, List<String>> qualifiers = getQualifiers(element);
		List<DatabaseReference> xrefs = parseXrefs(element);
		AnnotatedGene geneNames = getGeneName(qualifiers);
		if (qualifiers.containsKey("pseudo")) {
			List<Pseudogene> genes = findEnclosedFeatures(component
					.getPseudogenes(), loc);
			Pseudogene masterGene = null;
			for (Pseudogene gene : genes) {
				if (masterGene == null) {
					masterGene = gene;
				} else {
					// merge location
					masterGene.setLocation(ModelUtils.mergeLocations(masterGene
							.getLocation(), gene.getLocation()));
					// merge names
					masterGene.addAnnotatedGene(gene);
					component.getPseudogenes().remove(gene);
				}
			}
			if (masterGene != null) {
				// merge genes that are enclosed together
				masterGene.getDatabaseReferences().addAll(xrefs);
				masterGene.addAnnotatedGene(geneNames);
			} else {
				System.out.println("No pgene found for "+loc);
			}
		} else {
			// merge genes that are enclosed together into one
			Gene masterGene = null;
			for (Gene gene : findEnclosedFeatures(component.getGenes(), loc)) {
				if (masterGene == null) {
					masterGene = gene;
				} else {
					// merge location
					masterGene.setLocation(ModelUtils.mergeLocations(masterGene
							.getLocation(), gene.getLocation()));
					// merge proteins
					masterGene.getProteins().addAll(gene.getProteins());
					// merge names
					masterGene.addAnnotatedGene(gene);
					// remove all genes
					component.getGenes().remove(gene);
				}
			}
			if (masterGene != null) {
				masterGene.getDatabaseReferences().addAll(xrefs);
				masterGene.addAnnotatedGene(geneNames);
			} else {
				System.out.println("No gene found for "+loc);
			}
		}
	}

}
