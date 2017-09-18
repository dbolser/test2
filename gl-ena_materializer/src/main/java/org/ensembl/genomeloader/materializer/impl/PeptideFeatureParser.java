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

package org.ensembl.genomeloader.materializer.impl;

import java.util.ArrayList;
import java.util.List;

import org.ensembl.genomeloader.genomebuilder.model.Gene;
import org.ensembl.genomeloader.genomebuilder.model.ModelUtils;
import org.ensembl.genomeloader.genomebuilder.model.Protein;
import org.ensembl.genomeloader.genomebuilder.model.ProteinFeature;
import org.ensembl.genomeloader.genomebuilder.model.ProteinFeatureSource;
import org.ensembl.genomeloader.genomebuilder.model.ProteinFeatureType;
import org.ensembl.genomeloader.genomebuilder.model.impl.DelegatingEntityLocation;
import org.ensembl.genomeloader.genomebuilder.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.genomebuilder.model.impl.ProteinFeatureImpl;
import org.ensembl.genomeloader.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.util.biojava.FeatureLocationGapOverlapException;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.biojava.TripletLocationException;

import nu.xom.Element;

/**
 * Parser for features of type sig_peptide, transit_peptide, mat_peptide. These
 * are added as features to the proteins which enclose them
 * 
 * @author dstaines
 */
public class PeptideFeatureParser extends XmlEnaFeatureParser {

	public PeptideFeatureParser(DatabaseReferenceTypeRegistry registry) {
		super(registry);
	}

	@Override
	public void parseFeature(GenomicComponentImpl component, Element element) {

		ProteinFeature ft = new ProteinFeatureImpl();
		ft.setLocation(new DelegatingEntityLocation(parseLocation(element)));
		ft.setType(ProteinFeatureType.forEmblString(element
				.getAttributeValue("name")));
		boolean found = false;
		for (Gene gene : component.getGenes()) {
			for (Protein protein : gene.getProteins()) {
				if (protein.getLocation().contains(ft.getLocation())
						&& LocationUtils.overlapsInFrame(ft.getLocation(),
								protein.getLocation())) {
					getLog().debug(
							"Feature " + ft + " overlaps with CDS "
									+ protein.getIdString() + ": assigning");
					try {
						ModelUtils.setFeatureCoordinates(ft,
								protein.getLocation());
						ModelUtils.assignLocationModifiers(
								protein.getLocation(), ft.getLocation());
						ft.setSource(ProteinFeatureSource.GENOMIC_ANNOTATION);
						protein.addProteinFeature(ft);
						found = true;
						break;
					} catch (TripletLocationException e) {
						getLog().warn(
								"Feature " + ft + " from location "
										+ protein.getLocation()
										+ " is not triplet based");
					} catch (FeatureLocationGapOverlapException e) {
						getLog().warn(
								"Feature "
										+ ft
										+ " overlaps a gap in the genomic location "
										+ protein.getLocation());
					} finally {

					}
				}
			}
		}
		if (!found) {
			getLog().warn("Could not find protein for protein feature " + ft);
			getDefaultParser().parseFeature(component, element);
		}

	}

	@Override
	public List<Class<? extends XmlEnaFeatureParser>> dependsOn() {
		return new ArrayList<Class<? extends XmlEnaFeatureParser>>() {
			{
				add(CdsFeatureParser.class);
				add(GeneFeatureParser.class);
			}
		};
	}

}
