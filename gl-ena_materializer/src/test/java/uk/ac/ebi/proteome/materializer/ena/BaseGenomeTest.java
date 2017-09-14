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

import java.util.Arrays;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GeneImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomeImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.ProteinImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.TranscriptImpl;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;

public abstract class BaseGenomeTest {

    protected Genome getEmptyGenome(int length, Gene... genes) {
        Genome g = new GenomeImpl("1", 0, "", "");
        GenomicComponentImpl gc = new GenomicComponentImpl("A");
        gc.setMetaData(new GenomicComponentMetaData());
        gc.setLength(length);
        gc.getMetaData().setCircular(true);
        gc.getMetaData().setLength(length);
        gc.getGenes().addAll(Arrays.asList(genes));
        g.addGenomicComponent(gc);
        return g;
    }

    protected Gene buildCdsGene(String location, String id) {
        GeneImpl g1 = new GeneImpl();
        g1.setIdentifyingId(id);
        g1.setLocation(new DelegatingEntityLocation(LocationUtils.parseEmblLocation(location)));
        ProteinImpl p1 = new ProteinImpl();
        p1.setIdentifyingId(id + "P");
        p1.setLocation(new DelegatingEntityLocation(LocationUtils.parseEmblLocation(location)));
        g1.addProtein(p1);
        TranscriptImpl t1 = new TranscriptImpl();
        t1.setLocation(new DelegatingEntityLocation(LocationUtils.parseEmblLocation(location)));
        t1.setIdentifyingId(id + "P");
        p1.addTranscript(t1);
        t1.addProtein(p1);
        return g1;
    }

}
