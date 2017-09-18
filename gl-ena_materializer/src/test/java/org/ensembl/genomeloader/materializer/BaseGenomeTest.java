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

import java.util.Arrays;

import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.impl.DelegatingEntityLocation;
import org.ensembl.genomeloader.model.impl.GeneImpl;
import org.ensembl.genomeloader.model.impl.GenomeImpl;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.model.impl.ProteinImpl;
import org.ensembl.genomeloader.model.impl.TranscriptImpl;
import org.ensembl.genomeloader.util.biojava.LocationUtils;

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
