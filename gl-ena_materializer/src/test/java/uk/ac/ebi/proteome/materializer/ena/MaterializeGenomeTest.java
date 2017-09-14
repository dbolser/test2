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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Comparator;

import org.junit.Test;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.materializer.ena.identifiers.EnaComponentSetIdentifier;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

public class MaterializeGenomeTest {

    @Test
    public void testType0Id() throws Exception {
        GenomeMetaData gd = getMetaData("18");
        assertNotNull("GenomeMetaData", gd);
        assertEquals("Component size", 2, gd.getComponentMetaData().size());
        assertEquals("Genome ID", "18", gd.getIdentifier());
        Collections.sort(gd.getComponentMetaData(), new Comparator<GenomicComponentMetaData>() {

            public int compare(GenomicComponentMetaData o1, GenomicComponentMetaData o2) {
                return o1.getAccession().compareTo(o2.getAccession());
            }
        });
        assertEquals("First elem acc", gd.getComponentMetaData().get(0).getAccession(), "AP001918");
        assertEquals("Second elem acc", gd.getComponentMetaData().get(1).getAccession(), "U00096");
    }

    @Test
    public void testType0Mat() throws Exception {
        GenomeMetaData gd = getMetaData("18");
        EnaGenomeMaterializer mat = new EnaGenomeMaterializer(getConfig());
        Genome g = mat.materializeData(gd);
        assertNotNull("Genome", g);
        assertEquals("ID", g.getId(), "18");
        assertEquals("GC number", g.getGenomicComponents().size(), 2);
    }

    @Test
    public void testTypeAcOnlyMat() throws Exception {
        GenomeMetaData gd = new EnaComponentSetIdentifier(getConfig()).getMetaDataForIdentifier("32054",
                CollectionUtils.createArrayList("U00096", "AP001918"));
        EnaGenomeMaterializer mat = new AcOnlyEnaGenomeMaterializer(getConfig());
        Genome g = mat.materializeData(gd);
        assertNotNull("Genome", g);
        System.out.println(g);
        // assertEquals("ID", g.getId(), "18");
        assertEquals("GC number", g.getGenomicComponents().size(), 2);
    }

    private EnaGenomeConfig config;

    private EnaGenomeConfig getConfig() {
        if (config == null) {
            config = new EnaGenomeConfig();
            config.setProtUri("jdbc:oracle:thin:proteomes_prod/pprod@localhost:15310:PRPRO");
            config.setEnaUri("jdbc:oracle:thin:proteomes_prod/pprod@localhost:25310:PRDB1");
            config.setEnaXmlUrl("http://www.ebi.ac.uk/ena/data/view/$ac$&display=xml");
        }
        return config;
    }

    private GenomeMetaData getMetaData(String id) {
        EnaGenomeIdentifier idfer = new EnaGenomeIdentifier(ServiceContext.getInstance(), getConfig());
        GenomeMetaData gd = idfer.getMetaDataForIdentifier(id);
        return gd;
    }

}
