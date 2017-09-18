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

import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomeImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.executor.SimpleExecutor;
import uk.ac.ebi.proteome.materializer.ena.impl.XmlEnaComponentParser;
import uk.ac.ebi.proteome.materializer.ena.processors.GenomeProcessor;
import uk.ac.ebi.proteome.materializer.ena.processors.UpiGenomeProcessor;
import uk.ac.ebi.proteome.materializer.ena.processors.UpiInterproGenomeProcessor;
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.services.sql.impl.LocalSqlService;
import uk.ac.ebi.proteome.util.InputOutputUtils;

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
