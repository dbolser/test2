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

import org.biojavax.bio.seq.RichLocation.Strand;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.EntityLocation;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.Transcript;
import org.ensembl.genomeloader.model.impl.DelegatingEntityLocation;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

import nu.xom.Element;

/**
 * @author dstaines
 * 
 */
public class UtrFeatureParser extends XmlEnaFeatureParser {

	private final static String FIVE_UTR = "5'UTR";
	private final static String THREE_UTR = "3'UTR";

	public UtrFeatureParser(DatabaseReferenceTypeRegistry registry) {
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
				add(MrnaFeatureParser.class);
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
		// find genes with transcripts that abut the UTR and extend
		EntityLocation uLoc = parseLocation(element);
		List<DatabaseReference> xrefs = parseXrefs(element);
		boolean isFound = false;
		String nom = element.getAttribute("name").getValue();
		boolean isFive = FIVE_UTR.equals(nom);
		GENE: for (Gene gene : component.getGenes()) {
			for (Protein protein : gene.getProteins()) {
				for (Transcript transcript : protein.getTranscripts()) {
					EntityLocation tLoc = transcript.getLocation();
					if (tLoc.getStrand() == uLoc.getStrand()) {
						if (isFive) {
							if (tLoc.getStrand() == Strand.POSITIVE_STRAND) {
								isFound = tLoc.getMin() == uLoc.getMax() + 1;
							} else if (tLoc.getStrand() == Strand.NEGATIVE_STRAND) {
								isFound = tLoc.getMax() + 1 == uLoc.getMin();
							}
						} else {
							if (tLoc.getStrand() == Strand.POSITIVE_STRAND) {
								isFound = tLoc.getMax() + 1 == uLoc.getMin();
							} else if (tLoc.getStrand() == Strand.NEGATIVE_STRAND) {
								isFound = tLoc.getMin() == uLoc.getMax() + 1;
							}
						}
						if (isFound) {
							getLog().info(
									"Merging " + nom + " at " + uLoc
											+ " with transcript "
											+ transcript.getIdentifyingId()
											+ " at " + tLoc);
							EntityLocation eLoc = new DelegatingEntityLocation(
									LocationUtils.unite(tLoc, uLoc));
							eLoc.getInsertions().addAll(tLoc.getInsertions());
							eLoc.getExceptions().addAll(tLoc.getExceptions());
							transcript.setLocation(eLoc);
							transcript.getDatabaseReferences().addAll(xrefs);
							break GENE;
						}
					}
				}
			}
		}
		if (!isFound) {
			getLog().warn(
					"Could not find transcript for " + nom + " at " + uLoc);
			getDefaultParser().parseFeature(component, element);
		}
	}

}
