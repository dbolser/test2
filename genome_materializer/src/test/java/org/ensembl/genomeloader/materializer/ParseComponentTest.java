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

package org.ensembl.genomeloader.materializer;

/**
 * File: ParseFPlasmidTest.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.InputStream;

import org.ensembl.genomeloader.materializer.executor.SimpleExecutor;
import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.util.InputOutputUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import org.junit.Test;

/**
 * @author dstaines
 * 
 */
public class ParseComponentTest {

    private final DatabaseReferenceTypeRegistry reg = new XmlDatabaseReferenceTypeRegistry();

    @Test
    public void testAP001918() throws Exception {
        InputStream is = InputOutputUtils.openGzippedClasspathResource("/AP001918.xml.gz");
        EnaParser parser = new EnaParser(new SimpleExecutor(), reg);
        GenomeMetaData gmd = new GenomeMetaData("1", "", 0);
        GenomicComponent gc = parser.parse(new GenomicComponentMetaData("AP001918", gmd), is);
        assertNotNull(gc);
        assertEquals("AP001918", gc.getAccession());
        assertNotNull(gc.getMetaData());
        System.out.println("Genes=>" + gc.getGenes().size());
    }

    @Test
    public void testU00096() throws Exception {
        InputStream is = InputOutputUtils.openGzippedClasspathResource("/U00096.xml.gz");
        EnaParser parser = new EnaParser(new SimpleExecutor(), reg);
        GenomeMetaData gmd = new GenomeMetaData("1", "", 0);
        GenomicComponent gc = parser.parse(new GenomicComponentMetaData("U00096", gmd), is);
        assertNotNull(gc);
        assertEquals("U00096", gc.getAccession());
        assertNotNull(gc.getMetaData());
        System.out.println("Genes=>" + gc.getGenes().size());
    }

}
