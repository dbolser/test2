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

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.services.ServiceContext;

public class MaterializeEntry {

    public static final void main(String[] args) {
        Genome g = new MaterializeEntry().materialize(args[0]);
        System.out.println(g);
    }

    public Genome materialize(String id) {
        Genome g = null;
        GenomeMetaData gd = getMetaData(id);
        EnaGenomeMaterializer mat = new EnaGenomeMaterializer(getConfig());
        g = mat.materializeData(gd);
        return g;
    }

    private EnaGenomeConfig config;

    private EnaGenomeConfig getConfig() {
        if (config == null) {
            config = EnaGenomeConfig.getConfig();
        }
        return config;
    }

    private GenomeMetaData getMetaData(String id) {
        EnaGenomeIdentifier idfer = new EnaGenomeIdentifier(ServiceContext.getInstance(), getConfig());
        GenomeMetaData gd = idfer.getMetaDataForIdentifier(id);
        return gd;
    }

}
