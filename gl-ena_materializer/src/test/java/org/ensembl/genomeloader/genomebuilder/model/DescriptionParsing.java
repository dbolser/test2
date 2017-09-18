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

package org.ensembl.genomeloader.genomebuilder.model;

import static org.junit.Assert.assertEquals;

import org.ensembl.genomeloader.genomebuilder.metadata.GenomicComponentDescriptionHandler;
import org.ensembl.genomeloader.genomebuilder.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.genomebuilder.metadata.GenomicComponentMetaData.GenomicComponentType;
import org.ensembl.genomeloader.genomebuilder.metadata.impl.DefaultGenomicComponentDescriptionHandler;
import org.ensembl.genomeloader.genomebuilder.model.impl.GenomeInfoImpl;
import org.junit.Test;

public class DescriptionParsing {

	private static final String DEFAULT_ACC = "failed to parse";

	public void testDescription(String taxon, String input,
			GenomicComponentType expectedType, String expectedName) {
		final DefaultGenomicComponentDescriptionHandler handler = new DefaultGenomicComponentDescriptionHandler();
		final GenomicComponentMetaData md = new GenomicComponentMetaData();
		md.setComponentType(null);
		md.setAccession(DEFAULT_ACC);
		md.setGenomeInfo(new GenomeInfoImpl("dummy", 1, "wah", taxon));
		handler.parseComponentDescription(md, input);
		assertEquals(expectedType, md.getComponentType());
		assertEquals(expectedName, md.getName());
	}

	@Test
	public void testGiardia() {
		testDescription(
				"eukaryota",
				"Giardia lamblia ATCC 50803 strain WB C6 ctg02_235, whole genome shotgun sequence.",
				GenomicComponentType.SUPERCONTIG, "ctg02_235");
	}

	@Test
	public void testPichia() {
		testDescription("eukaryota",
				"Pichia pastoris GS115 chromosome 1, complete sequence",
				GenomicComponentType.CHROMOSOME, "1");
	}

	@Test
	public void testYeast() {
		testDescription(
				"eukaryota",
				"TPA: Saccharomyces cerevisiae S288c chromosome VII, complete sequence.",
				GenomicComponentType.CHROMOSOME, "VII");
	}

	@Test
	public void testYeastMt() {
		testDescription("eukaryota",
				"Saccharomyces cerevisiae complete mitochondrial genome",
				GenomicComponentType.CHROMOSOME,
				GenomicComponentDescriptionHandler.MITOCHONDRION);
	}

	@Test
	public void testBfucMt() {
		testDescription(
				"eukaryota",
				"Botryotinia fuckeliana B05.10 scaffold_589 mitochondrial scaffold, whole genome shotgun sequence.",
				GenomicComponentType.CHROMOSOME,
				GenomicComponentDescriptionHandler.MITOCHONDRION);
	}

	@Test
	public void testEcoli() {
		testDescription("eubacteria",
				"Escherichia coli str. K-12 substr. MG1655, complete genome.",
				GenomicComponentType.CHROMOSOME,
				GenomicComponentDescriptionHandler.CHROMOSOME);
	}

	@Test
	public void testEcoliF() {
		testDescription("eubacteria",
				"Plasmid F genomic DNA, complete sequence.",
				GenomicComponentType.PLASMID, "F");
	}

	@Test
	public void testOvis() {
		testDescription(
				"eukaryota",
				"Ovis aries breed Texel unplaced genomic scaffold scaffold003275, whole genome shotgun sequence.",
				GenomicComponentType.SUPERCONTIG, "scaffold003275");
	}

	@Test
	public void testCapra() {
		testDescription("eukaryota",
				"Capra hircus chromosome 4, whole genome shotgun sequence.",
				GenomicComponentType.CHROMOSOME, "4");
	}

	@Test
	public void testSalmonella() {
		testDescription(
				"eubacteria",
				"Salmonella enterica subsp. enterica serovar Typhi str. CT18, complete chromosome",
				GenomicComponentType.CHROMOSOME,
				GenomicComponentDescriptionHandler.CHROMOSOME);
	}

	@Test
	public void testBeta() {
		testDescription(
				"eukaryota",
				"Beta vulgaris subsp. vulgaris cultivar KWS2320 chromosome 1 genomic scaffold Bvchr1.sca001, whole genome shotgun sequence.",
				GenomicComponentType.SUPERCONTIG, "Bvchr1.sca001");
	}

	@Test
	public void testRalstonia() {
		testDescription("eubacteria",
				"Ralstonia solanacearum GMI1000 chromosome complete sequence",
				GenomicComponentType.CHROMOSOME,
				GenomicComponentDescriptionHandler.CHROMOSOME);
	}

	@Test
	public void testRalstoniaPlasmid() {
		testDescription("eubacteria",
				"Ralstonia solanacearum GMI1000 megaplasmid complete sequence",
				GenomicComponentType.PLASMID, "megaplasmid");
	}

	@Test
	public void test2MicronPlasmid() {
		testDescription(
				"eubacteria",
				"Saccharomyces cerevisiae R008 plasmid 2 micron, whole genome shotgun sequence.",
				GenomicComponentType.PLASMID, "2");
	}

	@Test
	public void testGlossinaAusteni() {
		// testing where there is no name!
		testDescription("eubacteria",
				"Glossina austeni, whole genome shotgun sequence.",
				GenomicComponentType.SUPERCONTIG, DEFAULT_ACC);
	}
	
	@Test
	public void testChromosomeWgs() {
		testDescription("eubacteria",
				"Saccharomyces cerevisiae AWRI796 chromosome II, whole genome shotgun sequence.",
				GenomicComponentType.CHROMOSOME, "II");
	}

	@Test
	public void testChromosomeWgsScaffold() {
		testDescription("eubacteria",
				"Saccharomyces cerevisiae AWRI796 chromosome II genomic scaffold AWRI796_chr_II_1, whole genome shotgun sequence.",
				GenomicComponentType.SUPERCONTIG, "AWRI796_chr_II_1");
	}

	@Test
	public void testLinkageGroup() {
		testDescription("banana",
				"Bombus terrestris linkage group LG B01, whole genome shotgun sequence.",
				GenomicComponentType.CHROMOSOME, "B01");
	}
	
	@Test
	public void testEnaChrDes() {
		testDescription("eubacteria",
				"Nocardia farcinica genome assembly NCTC11134, chromosome : 1",
				GenomicComponentType.CHROMOSOME, "1");
	}
	
	@Test
	public void testEnaChrDes2() {
		testDescription("eubacteria",
				"Nothobranchius furzeri genome assembly Nfu_20140520, chromosome : sgr01",
				GenomicComponentType.CHROMOSOME, "sgr01");
	}
	
	@Test
	public void testEnaPla() {
		testDescription("eubacteria",
	"Nocardia farcinica genome assembly NCTC11134, plasmid : 3",GenomicComponentType.PLASMID,"3");
	}
	
	@Test
	public void testEnaCon() {
		testDescription("eubacteria",
	"Nautella italica genome assembly N.italicaCECT7321_Prokka, contig 0001", GenomicComponentType.SUPERCONTIG, "0001");
	}
	
	@Test
	public void testEnaSeg() {
		testDescription("eubacteria",
	"Influenza A virus (A/England/8/2009(H1N1)) genome assembly A/England/8/2010, segment : 1", GenomicComponentType.SUPERCONTIG, "1");
	}
	
	@Test
	public void testEnaSca() {
		testDescription("eubacteria",
	"Influenza A virus (A/England/8/2009(H1N1)) genome assembly A/England/8/2010, scaffold : TEST", GenomicComponentType.SUPERCONTIG, "TEST");
	}

	
}
