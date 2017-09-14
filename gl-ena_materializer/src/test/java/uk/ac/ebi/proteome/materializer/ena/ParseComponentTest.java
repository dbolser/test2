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

import java.io.InputStream;

import org.junit.Test;

import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomeInfoImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.executor.SimpleExecutor;
import uk.ac.ebi.proteome.materializer.ena.impl.XmlEnaComponentParser;
import uk.ac.ebi.proteome.materializer.ena.impl.XmlEnaGenomeInfoParser;
import uk.ac.ebi.proteome.util.InputOutputUtils;

/**
 * @author dstaines
 * 
 */
public class ParseComponentTest {

	private final DatabaseReferenceTypeRegistry reg = new XmlDatabaseReferenceTypeRegistry();

	@Test
	public void testAP001918() throws Exception {
		InputStream is = InputOutputUtils
				.openGzippedClasspathResource("/AP001918.xml.gz");
		EnaParser<GenomicComponentImpl> parser = new XmlEnaComponentParser(new SimpleExecutor(),reg);
		GenomicComponent gc = parser.parse(is);
		assertNotNull(gc);
		assertEquals("AP001918", gc.getAccession());
		assertNotNull(gc.getMetaData());
		System.out.println("Genes=>" + gc.getGenes().size());
	}

	@Test
	public void testU00096() throws Exception {
		InputStream is = InputOutputUtils
				.openGzippedClasspathResource("/U00096.xml.gz");
		EnaParser<GenomicComponentImpl> parser = new XmlEnaComponentParser(new SimpleExecutor(),reg);
		GenomicComponent gc = parser.parse(is);
		assertNotNull(gc);
		assertEquals("U00096", gc.getAccession());
		assertNotNull(gc.getMetaData());
		System.out.println("Genes=>" + gc.getGenes().size());
	}

	@Test
	public void testU00096Src() throws Exception {
		InputStream is = InputOutputUtils
				.openGzippedClasspathResource("/U00096.xml.gz");
		EnaParser<GenomeInfo> parser = new XmlEnaGenomeInfoParser();
		GenomeInfo md = parser.parse(is);
	}

}
