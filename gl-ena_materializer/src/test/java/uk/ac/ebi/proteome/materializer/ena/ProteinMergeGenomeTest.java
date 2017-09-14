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

import java.util.Map;

import org.junit.Test;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DatabaseReferenceImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.processors.AltTranslationProcessor;
import uk.ac.ebi.proteome.materializer.ena.processors.GenomeProcessor;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Tests whether protein merge processor works properly
 * @author dstaines
 * 
 */
public class ProteinMergeGenomeTest extends BaseGenomeTest {

	private final DatabaseReferenceTypeRegistry reg = new XmlDatabaseReferenceTypeRegistry();

	@Test
	public void testSplitGenes() throws Exception {
		GenomeProcessor p = new AltTranslationProcessor(
				EnaGenomeConfig.getConfig(), reg);
		DatabaseReferenceType uniType = reg.getTypeForQualifiedName(
				"UniProtKB", "Swiss-Prot");
		Gene g1a = buildCdsGene("1..100", "ABC");
		Protein p1a = CollectionUtils.getFirstElement(g1a.getProteins(), null);
		Gene g2a = buildCdsGene("20..100", "ABC");
		Protein p2a = CollectionUtils.getFirstElement(g2a.getProteins(), null);
		g1a.addProtein(p2a);
		p1a.addDatabaseReference(new DatabaseReferenceImpl(uniType, "P12345"));
		p2a.addDatabaseReference(new DatabaseReferenceImpl(uniType, "P12345"));
		Genome g = getEmptyGenome(1000, g1a);
		GenomicComponent gc = CollectionUtils.getFirstElement(
				g.getGenomicComponents(), null);
		assertNotNull("GenomicComponent exists", gc);
		assertEquals("GenomicComponent count", 1, g.getGenomicComponents()
				.size());
		assertEquals("Gene count", 1, gc.getGenes().size());
		p.processGenome(g);
		assertEquals("Gene count", 1, gc.getGenes().size());
		Gene g1 = CollectionUtils.getFirstElement(gc.getGenes(), null);
		assertNotNull("Gene exists", g1);
		assertEquals("Gene ID", "ABC", g1.getIdentifyingId());
		assertEquals("Gene start", 1, g1.getLocation().getMin());
		assertEquals("Gene start", 100, g1.getLocation().getMax());
		assertEquals("Protein count", 2, g1.getProteins().size());
		Map<String, Transcript> map = CollectionUtils.createHashMap();
		for (Protein px : g1.getProteins()) {
			for (Transcript t : px.getTranscripts()) {
				assertEquals("Transcript start", 1, t.getLocation().getMin());
				map.put(t.getIdentifyingId(), t);
			}
		}
		assertEquals("Transcript count", 1, map.keySet().size());
	}

}
