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
package uk.ac.ebi.proteome.materializer.ena.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import nu.xom.Element;
import uk.ac.ebi.proteome.genomebuilder.model.AnnotatedGene;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.genomebuilder.model.GeneNameType;
import uk.ac.ebi.proteome.genomebuilder.model.Locatable;
import uk.ac.ebi.proteome.genomebuilder.model.ModelUtils;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Class to try and map mRNA features onto existing gene-protein-transcript sets. First pass is to find candidates with the same locus tag, and then to identify which mRNAs to match them to. Strategy is to work from smallest to largest until one found that contains the feature
 * 
 * @author dstaines
 * 
 */
public class MrnaFeatureParser extends XmlEnaFeatureParser {
	
	private final class MrnaComparator implements Comparator<Locatable> {
		
		private final int len;
		public MrnaComparator(int len) {
			this.len = len;
		}

		public int compare(Locatable o1, Locatable o2) {
			int cmp = Integer.valueOf(o1.getLocation().getMin()).compareTo(o2.getLocation().getMin());
			if(cmp==0) {
				Integer diff1 = len - LocationUtils.getLocationLength(o1.getLocation());
				Integer diff2 = len - LocationUtils.getLocationLength(o2.getLocation());
				cmp = diff1.compareTo(diff2);
			}
			return cmp;
		}

	};

	public MrnaFeatureParser(DatabaseReferenceTypeRegistry registry) {
		super(registry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.materializer.ena.impl.XmlEnaFeatureParser#dependsOn()
	 */
	public List<Class<? extends XmlEnaFeatureParser>> dependsOn() {
		return new ArrayList<Class<? extends XmlEnaFeatureParser>>() {
			{
				add(CdsFeatureParser.class);
				add(GeneFeatureParser.class);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.materializer.ena.impl.XmlEnaFeatureParser#parseFeature
	 * (uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl,
	 * nu.xom.Element)
	 */
	public void parseFeature(GenomicComponentImpl component, Element element) {
		// find genes matching these mRNAs
		// 1. with same locus tags
		// 2. with transcripts that are enclosed (not sure how to deal with
		// alternative splicing!)
		EntityLocation loc = parseLocation(element);
		Map<String, List<String>> qualifiers = getQualifiers(element);
		List<DatabaseReference> xrefs = parseXrefs(element);
		// get a reference
		DatabaseReference transcriptFeatureIdRef = getFeatureIdentifierRef(component,
				element, "TRANSCRIPT");
		String re = component
			.getMetaData().getAccession() +":" + component.getMetaData()
			.getVersion() + ":" + element.getAttributeValue("name") + ":.*";
		AnnotatedGene geneNames = getGeneName(qualifiers);
		List<GeneName> locusTags = geneNames.getNameMap().get(
				GeneNameType.ORDEREDLOCUSNAMES);
		// find candidate transcripts
		List<Transcript> candidates = CollectionUtils.createArrayList();
		for (Gene g : findGenesByName(component.getGenes(), locusTags)) {
			for (Protein p : g.getProteins()) {
				for (Transcript t : p.getTranscripts()) {
					if (LocationUtils.contains(loc, t.getLocation())) {
						// check to see if candidate transcript has already been modified to take an mRNA
						boolean found = false;
						for(DatabaseReference ref: ModelUtils.getReferencesForType(t, transcriptFeatureIdRef.getDatabaseReferenceType())) {
							if(ref.getPrimaryIdentifier().matches(re)) {
								found = true;
								break;
							}
						}
						if(!found) {
							candidates.add(t);
						}
					}
				}
			}
		}
		if (candidates.size() > 1) {
			getLog().warn(
					candidates.size() + " candidates found for " + locusTags
							+ ": taking 5' most");
			Collections.sort(candidates, new MrnaComparator(LocationUtils.getLocationLength(loc)));
		}
		Transcript t = CollectionUtils.getFirstElement(candidates, null);
		if (t == null) {
			getLog().warn("No candidate transcript found for mRNA at "+loc+": handling as a simple feature");
			getDefaultParser().parseFeature(component, element);
		} else {
			getLog().info(
					"Replacing location of transcript " + locusTags + " at "
							+ t.getLocation() + " with mRNA location " + loc);
			t.setLocation(loc);
			t.addDatabaseReference(transcriptFeatureIdRef);
		}

	}

}
