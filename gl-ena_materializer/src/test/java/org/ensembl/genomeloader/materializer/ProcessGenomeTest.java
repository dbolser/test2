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

import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.EnaParser;
import org.ensembl.genomeloader.materializer.executor.SimpleExecutor;
import org.ensembl.genomeloader.materializer.impl.XmlEnaComponentParser;
import org.ensembl.genomeloader.materializer.processors.GenomeProcessor;
import org.ensembl.genomeloader.materializer.processors.UpiGenomeProcessor;
import org.ensembl.genomeloader.materializer.processors.UpiInterproGenomeProcessor;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.impl.GenomeImpl;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.services.sql.impl.LocalSqlService;
import org.ensembl.genomeloader.util.InputOutputUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import org.junit.Test;

/**
 * @author dstaines
 * 
 */
public class ProcessGenomeTest {

    private final DatabaseReferenceTypeRegistry reg = new XmlDatabaseReferenceTypeRegistry();

    private Genome getGenome(String ac) {
        final InputStream is = InputOutputUtils.openGzippedClasspathResource("/" + ac + ".xml.gz");
        final EnaParser<GenomicComponentImpl> parser = new XmlEnaComponentParser(new SimpleExecutor(), reg);
        final GenomicComponent gc = parser.parse(is);
        assertNotNull(gc);
        assertEquals(ac, gc.getAccession());
        assertNotNull(gc.getMetaData());
        final Genome g = new GenomeImpl("1", 0, "", "");
        g.addGenomicComponent(gc);
        return g;
    }

    // @Test
    // public void testUpiProcessor() throws Exception {
    // GenomeProcessor p = new UpiGenomeProcessor(EnaGenomeConfig.getConfig(),
    // reg);
    // Genome g = getGenome("AP001918");
    // p.processGenome(g);
    // g = getGenome("U00096");
    // p.processGenome(g);
    //
    // }

    @Test
    public void testIproProcessor() throws Exception {

        SqlService srv = new LocalSqlService();
        final GenomeProcessor p = new UpiGenomeProcessor(EnaGenomeConfig.getConfig(), srv, reg);
        final GenomeProcessor p2 = new UpiInterproGenomeProcessor(EnaGenomeConfig.getConfig(), srv, reg);
        // Genome g = getGenome("AP001918");
        // p.processGenome(g);
        // p2.processGenome(g);
        final Genome g = getGenome("U00096");
        p.processGenome(g);
        p2.processGenome(g);
    }

}
