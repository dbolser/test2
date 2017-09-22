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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.genomeloader.materializer.EnaParsingException;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.model.AnnotatedGene;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.EntityLocation;
import org.ensembl.genomeloader.model.EntityLocationException;
import org.ensembl.genomeloader.model.GeneName;
import org.ensembl.genomeloader.model.GeneNameType;
import org.ensembl.genomeloader.model.ModelUtils;
import org.ensembl.genomeloader.model.impl.DatabaseReferenceImpl;
import org.ensembl.genomeloader.model.impl.GeneImpl;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.model.impl.ProteinImpl;
import org.ensembl.genomeloader.model.impl.TranscriptImpl;
import org.ensembl.genomeloader.model.sequence.SequenceUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

import nu.xom.Element;

/**
 * @author dstaines
 * 
 */
public class CdsFeatureParser extends XmlEnaFeatureParser {
	
	private static final String EC_NUMBER = "EC_number";
	private static final String CODON_START = "codon_start";
	private static final String TRANSL_EXCEPTION = "transl_except";
	private static final String TRANSL_TABLE = "transl_table";
	private final DatabaseReferenceType pidType;
	private final DatabaseReferenceType ecType;

	public CdsFeatureParser(DatabaseReferenceTypeRegistry registry) {
		super(registry);
		pidType = registry.getTypeForQualifiedName("EMBL", "PROTEIN");
		ecType = registry
				.getTypeForQualifiedName("EnzymeCommission", "PROTEIN");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.materializer.ena.impl.XmlEnaFeatureParser#dependsOn()
	 */
	public List<Class<? extends XmlEnaFeatureParser>> dependsOn() {
		return Collections.emptyList();
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
		GeneImpl gene = buildGene(component, element, qualifiers);
		getLog().debug(
				"Adding gene " + gene.getIdentifyingId()
						+ " from CDS to component "
						+ component.getMetaData().getAccession());
		component.addGene(gene);
	}

	private final Pattern exceptPattern = Pattern
			.compile("\\(pos:([0-9]+)..([0-9]+),aa:([A-Za-z]+)\\)");

	protected void addExceptions(EntityLocation loc,
			Map<String, List<String>> qualifiers) {
		// add exceptions
		if (qualifiers.containsKey(TRANSL_EXCEPTION)) {
			for (String exceptStr : qualifiers.get(TRANSL_EXCEPTION)) {
				Matcher m = exceptPattern.matcher(exceptStr);
				if (m.matches()) {
					String seq = SequenceUtils.aminoAcidTlaToSingle(m.group(3));
					loc.addException(new EntityLocationException(Integer
							.parseInt(m.group(1)),
							Integer.parseInt(m.group(2)), seq, seq));
				} else {
					getLog().warn("Cannot parse transl_except " + exceptStr);
				}
			}
		}
	}

	protected EntityLocation getLocation(Element element,
			Map<String, List<String>> qualifiers) {
		EntityLocation loc = parseLocation(element);
		addExceptions(loc, qualifiers);
		return loc;
	}

	protected GeneImpl buildGene(GenomicComponentImpl component,
			Element element, Map<String, List<String>> qualifiers) {

		// get a reference
		DatabaseReference geneFeatureIdRef = getFeatureIdentifierRef(component,
				element, "GENE");

		// create a Gene-Protein-Transcript set
		GeneImpl gene = new GeneImpl();
		AnnotatedGene geneName = getGeneName(qualifiers);
		List<GeneName> lts = geneName.getNameMap().get(
				GeneNameType.ORDEREDLOCUSNAMES);
		if (lts == null || lts.size() == 0) {
			getLog().warn(
					"Feature " + geneFeatureIdRef.getPrimaryIdentifier()
							+ " has no locus_tag qualifier");
		}
		gene.addAnnotatedGene(geneName);
		List<GeneName> noms = geneName.getNameMap().get(GeneNameType.NAME);
        if(noms!=null && !noms.isEmpty()) {
		    gene.setName(noms.get(0).getName());
		}
		
		gene.setDescription(getDescription(qualifiers));
		ProteinImpl protein = new ProteinImpl();
		boolean isFrameshift = hasNote(qualifiers,"contains frameshift") || hasNote(qualifiers,".*sequencing error.*");
		protein.setPseudo(qualifiers.containsKey("pseudo") || qualifiers.containsKey("pseudogene") || isFrameshift);
		gene.addProtein(protein);
        if(protein.isPseudo()) {
            gene.setBiotype("pseudogene");
        } else {
            gene.setBiotype("protein_coding");
        }
		TranscriptImpl transcript = new TranscriptImpl();
		protein.addTranscript(transcript);
		transcript.addProtein(protein);

		gene.addDatabaseReference(geneFeatureIdRef);
		protein.addDatabaseReference(getFeatureIdentifierRef(component,
				element, "PROTEIN"));
		transcript.addDatabaseReference(getFeatureIdentifierRef(component,
				element, "TRANSCRIPT"));

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
			default:
				throw new EnaParsingException("Unknown cross-reference type "
						+ xref.getDatabaseReferenceType().getType());
			}
		}

		if (qualifiers.containsKey(EC_NUMBER)) {
			for (String ecNumber : qualifiers.get(EC_NUMBER)) {
				protein.addDatabaseReference(new DatabaseReferenceImpl(ecType,
						ecNumber));
			}
		}

		String gId = getGeneId(gene);
		if (gId == null) {
			getLog().warn(
					"Feature " + geneFeatureIdRef.getPrimaryIdentifier()
							+ " has no suitable identifyingId");
		}
		DatabaseReference pidRef = getProteinId(qualifiers, geneFeatureIdRef);
		if (pidRef != null) {
			protein.addDatabaseReference(pidRef);
			protein.setName(pidRef.getSecondaryIdentifier());
			protein.setIdentifyingId(pidRef.getPrimaryIdentifier());
			transcript.setIdentifyingId(pidRef.getPrimaryIdentifier());
		} else {
			protein.setIdentifyingId(gId);
			protein.setName(gId);
			transcript.setIdentifyingId(gId);
		}
		gene.setIdentifyingId(gId);
		// take care of codon start
		if (qualifiers.containsKey(CODON_START)) {
			String codonStart = CollectionUtils.getFirstElement(
					qualifiers.get(CODON_START), null);
			protein.setCodonStart(Integer.parseInt(codonStart));
		}

		// check transl_table
		if (qualifiers.containsKey(TRANSL_TABLE)) {
			String translTable = CollectionUtils.getFirstElement(
					qualifiers.get(TRANSL_TABLE), null);
			if (translTable != null) {
				int translTableI = Integer.parseInt(translTable);
				if (component.getMetaData().getGeneticCode() == GenomicComponentMetaData.NULL_GENETIC_CODE) {
					component.getMetaData().setGeneticCode(translTableI);
				} else if (component.getMetaData().getGeneticCode() != translTableI) {
					throw new EnaParsingException(
							"CDS "
									+ pidRef.getPrimaryIdentifier()
									+ " contains transl_table="
									+ translTable
									+ " which conflicts with the previously identified table "
									+ component.getMetaData().getGeneticCode());
				}
			}
		}
		return gene;
	}

	protected DatabaseReference getProteinId(
			Map<String, List<String>> qualifiers,
			DatabaseReference geneFeatureIdRef) {
		List<String> proteinIds = qualifiers.get("protein_id");
		DatabaseReferenceImpl pidRef = null;
		if (proteinIds.size() == 1) {
			String pId = proteinIds.get(0);
			pidRef = new DatabaseReferenceImpl(pidType,
					ModelUtils.removeProteinIdVersion(pId), pId);
			pidRef.setDescription(geneFeatureIdRef.getPrimaryIdentifier());
		} else if (proteinIds.size() > 1) {
			throw new EnaParsingException(
					"Multiple protein_ids found for CDS: " + proteinIds);
		}
		return pidRef;
	}

}
