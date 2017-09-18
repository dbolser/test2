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
import org.ensembl.genomeloader.model.GeneName;
import org.ensembl.genomeloader.model.GeneNameType;
import org.ensembl.genomeloader.model.ModelUtils;
import org.ensembl.genomeloader.model.RnaTranscript;
import org.ensembl.genomeloader.model.Rnagene;
import org.ensembl.genomeloader.model.impl.GeneImpl;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.model.impl.ProteinImpl;
import org.ensembl.genomeloader.model.impl.TranscriptImpl;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

import nu.xom.Element;

/**
 * @author dstaines
 * 
 */
public class GeneFeatureParser extends CdsFeatureParser {

	public GeneFeatureParser(DatabaseReferenceTypeRegistry registry) {
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
				add(RnaFeatureParser.class);
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
		List<GeneName> locusTags = geneNames.getNameMap().get(
				GeneNameType.ORDEREDLOCUSNAMES);

		Gene masterGene = null;
		for (Gene gene : findGenesByName(component.getGenes(), locusTags)) {
			if (masterGene == null) {
				masterGene = gene;
			} else if (locationsOverlap(masterGene.getLocation(),
					gene.getLocation())) {
				// merge location
				masterGene.setLocation(ModelUtils.mergeLocations(
						masterGene.getLocation(), gene.getLocation()));
				// merge proteins
				masterGene.getProteins().addAll(gene.getProteins());
				// merge names
				masterGene.addAnnotatedGene(gene);
				masterGene.getDatabaseReferences().addAll(
						gene.getDatabaseReferences());
				// remove all genes
				component.getGenes().remove(gene);
			}
		}
		if (masterGene != null) {
			masterGene.setLocation(ModelUtils.mergeLocations(
					masterGene.getLocation(), loc));
			masterGene.getDatabaseReferences().addAll(xrefs);
			masterGene.addAnnotatedGene(geneNames);
		} else {
			getLog().debug("No protein coding gene found for " + locusTags);
			// try to find an RNA gene instead
			Rnagene masterRnaGene = null;
			List<Rnagene> rnaGenes = findGenesByName(component.getRnagenes(),
					locusTags);
			for (Rnagene rnaGene : rnaGenes) {
				if (masterRnaGene == null) {
					masterRnaGene = rnaGene;
				} else if (locationsOverlap(loc, rnaGene.getLocation())) {
					// if it falls into the overall gene location, merge the
					// sublocations
					masterRnaGene
							.setLocation(ModelUtils.mergeLocations(
									masterRnaGene.getLocation(),
									rnaGene.getLocation()));
					for(RnaTranscript t: masterRnaGene.getTranscripts()) {
						t.setLocation(masterRnaGene.getLocation());
					}
					// merge names
					masterRnaGene.addAnnotatedGene(rnaGene);
					masterRnaGene.getDatabaseReferences().addAll(
							rnaGene.getDatabaseReferences());
					// remove all genes
					component.getRnagenes().remove(rnaGene);
				}
			}
			if (masterRnaGene == null) {
				
				boolean isFrameshift = hasNote(qualifiers,"contains frameshift") || hasNote(qualifiers,".*sequencing error.*");
				if (qualifiers.containsKey("pseudo") || qualifiers.containsKey("pseudogene") || isFrameshift) {
					
					// get a reference
					DatabaseReference geneFeatureIdRef = getFeatureIdentifierRef(
							component, element, "GENE");

					// create a Gene-Protein-Transcript set
					GeneImpl gene = new GeneImpl();
					gene.addAnnotatedGene(geneNames);
					gene.setDescription(getDescription(qualifiers));
					ProteinImpl protein = new ProteinImpl();
					protein.setPseudo(qualifiers.containsKey("pseudo")||isFrameshift);
					gene.addProtein(protein);
					gene.setPseudogene(protein.isPseudo());
					TranscriptImpl transcript = new TranscriptImpl();
					protein.addTranscript(transcript);
					transcript.addProtein(protein);

					gene.addDatabaseReference(geneFeatureIdRef);
					protein.addDatabaseReference(getFeatureIdentifierRef(
							component, element, "PROTEIN"));
					transcript.addDatabaseReference(getFeatureIdentifierRef(
							component, element, "TRANSCRIPT"));

					gene.setLocation(getLocation(element, qualifiers));
					protein.setLocation(getLocation(element, qualifiers));
					transcript.setLocation(getLocation(element, qualifiers));

					// attach xrefs
					for (DatabaseReference xref : parseXrefs(element)) {
						switch (xref.getDatabaseReferenceType().getType()) {
						case GENE:
							gene.addDatabaseReference(xref);
							break;
						case PROTEIN:
							protein.addDatabaseReference(xref);
							break;
						case TRANSCRIPT:
							transcript.addDatabaseReference(xref);
							break;
						}
					}
					
				} else {
					
					getLog().warn(
							"No existing genes found for " + locusTags
									+ " from " + loc
									+ " - handling as a simple feature");
					getDefaultParser().parseFeature(component, element);
					
				}
			}
		}
	}

	

}
