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
 * XmlEnaFeatureParserComparatorTest
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package uk.ac.ebi.proteome.materializer.ena;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.impl.CdsFeatureParser;
import uk.ac.ebi.proteome.materializer.ena.impl.DefaultXmlEnaFeatureParser;
import uk.ac.ebi.proteome.materializer.ena.impl.GeneFeatureParser;
import uk.ac.ebi.proteome.materializer.ena.impl.MrnaFeatureParser;
import uk.ac.ebi.proteome.materializer.ena.impl.SourceFeatureParser;
import uk.ac.ebi.proteome.materializer.ena.impl.XmlEnaComponentParser;
import uk.ac.ebi.proteome.materializer.ena.impl.XmlEnaFeatureParser;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.collections.CollectionUtils.TopologicalComparator;

/**
 * @author dstaines
 * 
 */
public class XmlEnaFeatureParserComparatorTest {

	@Test
	public void test() {
		DatabaseReferenceTypeRegistry registry = new XmlDatabaseReferenceTypeRegistry();
		MrnaFeatureParser mrna = new MrnaFeatureParser(registry);
		GeneFeatureParser gene = new GeneFeatureParser(registry);
		CdsFeatureParser cds = new CdsFeatureParser(registry);
		SourceFeatureParser src = new SourceFeatureParser(registry);
		DefaultXmlEnaFeatureParser def = new DefaultXmlEnaFeatureParser(
				registry);
		List<XmlEnaFeatureParser> list = Arrays
				.asList(new XmlEnaFeatureParser[] { mrna, gene, cds, def, src });
		XmlEnaComponentParser.XmlEnaFeatureParserComparator cmp = new XmlEnaComponentParser.XmlEnaFeatureParserComparator();
		Collections.sort(list, cmp);
		assertTrue(firstBeforeSecond(list,cds,mrna));
		assertTrue(firstBeforeSecond(list,cds,gene));
		list = Arrays.asList(new XmlEnaFeatureParser[] { src, mrna, cds, def,
				gene });
		Collections.sort(list, cmp);
		assertTrue(firstBeforeSecond(list,cds,mrna));
		assertTrue(firstBeforeSecond(list,cds,gene));
		list = Arrays.asList(new XmlEnaFeatureParser[] { cds, src, def, gene,
				mrna });
		Collections.sort(list, cmp);
		assertTrue(firstBeforeSecond(list,cds,mrna));
		assertTrue(firstBeforeSecond(list,cds,gene));
	}
	
	private static boolean firstBeforeSecond(List<XmlEnaFeatureParser> list, XmlEnaFeatureParser one, XmlEnaFeatureParser two) {
		boolean success = true;
		boolean oneFound = false;
		for(XmlEnaFeatureParser elem: list) {
			if(elem.equals(one)) {
				oneFound = true;
			} else if(elem.equals(two) && !oneFound) {
				success = false;
				break;
			}
		}
		return success;
	}
	@Test
	public void testTopo() {
		DatabaseReferenceTypeRegistry registry = new XmlDatabaseReferenceTypeRegistry();
		MrnaFeatureParser mrna = new MrnaFeatureParser(registry);
		GeneFeatureParser gene = new GeneFeatureParser(registry);
		CdsFeatureParser cds = new CdsFeatureParser(registry);
		SourceFeatureParser src = new SourceFeatureParser(registry);
		DefaultXmlEnaFeatureParser def = new DefaultXmlEnaFeatureParser(
				registry);
		CollectionUtils.TopologicalComparator<XmlEnaFeatureParser> cmp = new CollectionUtils.TopologicalComparator<XmlEnaFeatureParser>() {
			public int countEdges(XmlEnaFeatureParser t) {
				return t.dependsOn().size();
			}
			public boolean hasEdge(XmlEnaFeatureParser from,
					XmlEnaFeatureParser to) {
				return from.dependsOn().contains(to.getClass());
			}
		};
		List<XmlEnaFeatureParser> list = Arrays
				.asList(new XmlEnaFeatureParser[] { mrna, gene, cds, def, src });
		list = CollectionUtils.topoSort(list, cmp);
		Collections.reverse(list);
		assertTrue(firstBeforeSecond(list,cds,mrna));
		assertTrue(firstBeforeSecond(list,cds,gene));
		list = Arrays.asList(new XmlEnaFeatureParser[] { src, mrna, cds, def,
				gene });
		list = CollectionUtils.topoSort(list, cmp);
		Collections.reverse(list);
		assertTrue(firstBeforeSecond(list,cds,mrna));
		assertTrue(firstBeforeSecond(list,cds,gene));
		list = Arrays.asList(new XmlEnaFeatureParser[] { cds, src, def, gene,
				mrna });
		list = CollectionUtils.topoSort(list, cmp);
		Collections.reverse(list);
		assertTrue(firstBeforeSecond(list,cds,mrna));
		assertTrue(firstBeforeSecond(list,cds,gene));
	}
}
