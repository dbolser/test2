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
import uk.ac.ebi.proteome.services.ServiceContext;

public class MaterializeGenome {

    public static final void main(String[] args) throws Exception {
        for (String ac : args) {
            System.out.println("Identifying " + ac);
            GenomeMetaData gd = getMetaData(ac);
            if (gd == null) {
                System.err.println("Cannot find genome " + ac);
            } else {
                EnaGenomeMaterializer mat = new EnaGenomeMaterializer(getConfig());
                mat.materializeData(gd);
            }
        }
    }

    private static EnaGenomeConfig config;

    private static EnaGenomeConfig getConfig() {
        if (config == null) {
            config = EnaGenomeConfig.getConfig();
        }
        return config;
    }

    private static GenomeMetaData getMetaData(String id) {
        EnaGenomeIdentifier idfer = new EnaGenomeIdentifier(ServiceContext.getInstance(), getConfig());
        GenomeMetaData gd = idfer.getMetaDataForIdentifier(id);
        return gd;
    }

}
