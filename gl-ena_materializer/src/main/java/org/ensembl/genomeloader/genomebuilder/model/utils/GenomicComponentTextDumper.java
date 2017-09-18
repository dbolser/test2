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
 * File: ComponentTextDumper.java
 * Created by: dstaines
 * Created on: Nov 12, 2008
 * CVS:  $$
 */
package org.ensembl.genomeloader.genomebuilder.model.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.ensembl.genomeloader.genomebuilder.model.DatabaseReference;
import org.ensembl.genomeloader.genomebuilder.model.EntityLocation;
import org.ensembl.genomeloader.genomebuilder.model.Gene;
import org.ensembl.genomeloader.genomebuilder.model.GenomicComponent;
import org.ensembl.genomeloader.genomebuilder.model.Protein;
import org.ensembl.genomeloader.genomebuilder.model.ProteinFeature;
import org.ensembl.genomeloader.genomebuilder.model.Pseudogene;
import org.ensembl.genomeloader.genomebuilder.model.Transcript;

/**
 * Simple dumper code for dumping a component out in a readable-ish file format
 *
 * @author dstaines
 *
 */
public class GenomicComponentTextDumper {
	public static final String COMPONENT_LINE = "ID  ";
	public static final String GENE_LINE = "GE  ";
	public static final String PROTEIN_LINE = "PR  ";
	public static final String PROTEIN_FT_LINE = "PRF ";
	public static final String TRANSCRIPT_LINE = "TR  ";
	public static final String OPERON_LINE = "OP  ";
	public static final String LOCATION_LINE = "LOC ";
	public static final String REFERENCE_LINE = "DR  ";
	public static final String PSEUDOGENE_LINE = "PG  ";

	public static void dumpGenomicComponent(File outFile,
			GenomicComponent component) throws IOException {
		dumpGenomicComponent(component, new FileWriter(outFile));
	}

	public static String dumpGenomicComponent(GenomicComponent component)
			throws IOException {
		StringWriter writer = new StringWriter();
		dumpGenomicComponent(component, writer);
		return writer.toString();
	}

	public static void dumpGenomicComponent(GenomicComponent component,
			Writer writer) throws IOException {
		new GenomicComponentTextDumper().dump(component, writer);
	}

	public GenomicComponentTextDumper() {
	}

	public void dump(GenomicComponent component, Writer writer)
			throws IOException {
		writer.append(COMPONENT_LINE);
		writer.append(component.getIdString());
		writer.append('\n');
		for (Gene gene : component.getGenes()) {
			appendGene(gene, writer);
			writer.append('\n');
		}
		for (Pseudogene gene : component.getPseudogenes()) {
			appendPseudoGene(gene, writer);
			writer.append('\n');
		}
		writer.flush();
	}

	protected void appendPseudoGene(Pseudogene gene, Writer writer)
			throws IOException {
		writer.append(PSEUDOGENE_LINE);
		writer.append(gene.getIdString());
		writer.append(' ');
		writer.append(gene.getType().toString());
		writer.append('\n');
		if (gene.getLocation() != null)
			appendLocation(gene.getLocation(), writer);
		for (DatabaseReference ref : gene.getDatabaseReferences()) {
			appendReference(ref, writer);
		}
	}

	protected void appendGene(Gene gene, Writer writer) throws IOException {
		writer.append(GENE_LINE);
		writer.append(gene.getIdString());
		if (gene.getPublicId() != null)
			writer.append(",igi=" + gene.getPublicId());
		writer.append('\n');
		if (gene.getLocation() != null)
			appendLocation(gene.getLocation(), writer);
		for (DatabaseReference ref : gene.getDatabaseReferences()) {
			appendReference(ref, writer);
		}
		for (Protein protein : gene.getProteins()) {
			appendProtein(protein, writer);
		}
	}

	protected void appendLocation(EntityLocation location, Writer writer)
			throws IOException {
		writer.append(LOCATION_LINE);
		writer.append(location.toString());
		writer.append('\n');
	}

	protected void appendProtein(Protein protein, Writer writer)
			throws IOException {
		writer.append(PROTEIN_LINE);
		writer.append(protein.getIdString());
		writer.append('\n');
		if (protein.getLocation() != null)
			appendLocation(protein.getLocation(), writer);
		for (DatabaseReference ref : protein.getDatabaseReferences()) {
			appendReference(ref, writer);
		}
		for (ProteinFeature ft : protein.getProteinFeatures()) {
			writer.append(PROTEIN_FT_LINE);
			writer.append(ft.toString());
			writer.append('\n');
		}
		for (Transcript transcript : protein.getTranscripts()) {
			appendTranscript(transcript, writer);
		}
	}

	protected void appendTranscript(Transcript transcript, Writer writer)
			throws IOException {
		writer.append(TRANSCRIPT_LINE);
		writer.append(transcript.toString());
		writer.append('\n');
		for (DatabaseReference ref : transcript.getDatabaseReferences()) {
			appendReference(ref, writer);
		}
		if (transcript.getOperon() != null) {
			writer.append(OPERON_LINE);
			writer.append(transcript.getOperon().toString());
			writer.append('\n');
		}
	}

	protected void appendReference(DatabaseReference ref, Writer writer)
			throws IOException {
		writer.append(REFERENCE_LINE);
		writer.append(ref.toString());
		writer.append('\n');
	}

}
