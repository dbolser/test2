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

package uk.ac.ebi.proteome.materializer.ena;

/**
 * File: ParseFPlasmidTest.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.ModelUtils;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.processors.GenomeProcessor;
import uk.ac.ebi.proteome.materializer.ena.processors.LocusTagMergeProcessor;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * @author dstaines
 * 
 */
public class LocusTagMergeGenomeTest extends BaseGenomeTest {

	private final DatabaseReferenceTypeRegistry reg = new XmlDatabaseReferenceTypeRegistry();

	@Test
	public void testSplitGenes() throws Exception {
		final GenomeProcessor p = new LocusTagMergeProcessor(EnaGenomeConfig.getConfig(),
				reg);
		final Genome g = getEmptyGenome(1000,buildCdsGene("1..100", "ABC"), buildCdsGene("900..1000", "ABC"));
		final GenomicComponent gc = CollectionUtils.getFirstElement(g.getGenomicComponents(), null);
		assertNotNull("GenomicComponent exists",gc);
		assertEquals("GenomicComponent count",1,g.getGenomicComponents().size());
		assertEquals("Gene count",2,gc.getGenes().size());
		p.processGenome(g);
		assertEquals("Gene count",1,gc.getGenes().size());
		final Gene g1 = CollectionUtils.getFirstElement(gc.getGenes(), null);
		assertNotNull("Gene exists",g1);
		assertEquals("Gene ID","ABC",g1.getIdentifyingId());
		assertEquals("Gene start",900,g1.getLocation().getMin());
		assertEquals("Gene start",100,g1.getLocation().getMax());
		assertEquals("Protein count",1,g1.getProteins().size());
	}
	
	@Test
	public void testOverlappingGenes() throws Exception {
		final GenomeProcessor p = new LocusTagMergeProcessor(EnaGenomeConfig.getConfig(),
				reg);
		final Genome g = getEmptyGenome(1000,buildCdsGene("1..100", "ABC"), buildCdsGene("20..100", "ABC"));
		final GenomicComponent gc = CollectionUtils.getFirstElement(g.getGenomicComponents(), null);
		assertNotNull("GenomicComponent exists",gc);
		assertEquals("GenomicComponent count",1,g.getGenomicComponents().size());
		assertEquals("Gene count",2,gc.getGenes().size());
		p.processGenome(g);
		assertEquals("Gene count",1,gc.getGenes().size());
		final Gene g1 = CollectionUtils.getFirstElement(gc.getGenes(), null);
		assertNotNull("Gene exists",g1);
		assertEquals("Gene ID","ABC",g1.getIdentifyingId());
		assertEquals("Gene start",1,g1.getLocation().getMin());
		assertEquals("Gene start",100,g1.getLocation().getMax());
		assertEquals("Protein count",2,g1.getProteins().size());
	}

	@Test
	public void testNonOverlappingGenes() throws Exception {
		final GenomeProcessor p = new LocusTagMergeProcessor(EnaGenomeConfig.getConfig(),
				reg);
		final Genome g = getEmptyGenome(1000,buildCdsGene("1..100", "ABC"), buildCdsGene("200..300", "ABC"));
		final GenomicComponent gc = CollectionUtils.getFirstElement(g.getGenomicComponents(), null);
		assertNotNull("GenomicComponent exists",gc);
		assertEquals("GenomicComponent count",1,g.getGenomicComponents().size());
		assertEquals("Gene count",2,gc.getGenes().size());
		p.processGenome(g);
		assertEquals("Gene count",2,gc.getGenes().size());
		final List<Gene> gs = CollectionUtils.createArrayList();
		gs.addAll(gc.getGenes());
		Collections.sort(gs, new ModelUtils.LocatableComparatorMinOnly());
		final Gene g1 = gs.get(0);
		assertNotNull("Gene exists",g1);
		assertEquals("Gene ID", "ABC", g1.getIdentifyingId());
		assertEquals("Gene start",1,g1.getLocation().getMin());
		assertEquals("Gene start",100,g1.getLocation().getMax());
		assertEquals("Protein count",1,g1.getProteins().size());
		final Gene g2 = gs.get(1);
		assertNotNull("Gene exists",g2);
		assertEquals("Gene ID", "ABC", g2.getIdentifyingId());
		assertEquals("Gene start",200,g2.getLocation().getMin());
		assertEquals("Gene start",300,g2.getLocation().getMax());
		assertEquals("Protein count",1,g2.getProteins().size());
	}

	
}
