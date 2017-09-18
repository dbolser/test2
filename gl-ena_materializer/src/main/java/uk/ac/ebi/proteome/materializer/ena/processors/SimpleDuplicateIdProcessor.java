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

package uk.ac.ebi.proteome.materializer.ena.processors;

import java.util.Map;
import java.util.Set;

import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.Identifiable;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.materializer.ena.EnaParsingException;

/**
 * {@link GenomeProcessor} to identify where IDs are duplicated for a given
 * region, and nullify them for future replacement
 * 
 * @author dstaines
 * 
 */
public class SimpleDuplicateIdProcessor extends DuplicateIdProcessor {

    public SimpleDuplicateIdProcessor(EnaGenomeConfig config) {
        super(config);
    }

    @Override
    protected int handleDuplicates(Genome genome, Map<String, Set<Identifiable>> dups) {
        boolean allowDuplicates = isAllowDuplicates(genome);
        int nDuplicate = 0;
        for (Set<Identifiable> ids : dups.values()) {
            if (ids.size() > 1) {
                for (Identifiable id : ids) {
                    if (allowDuplicates) {
                        nDuplicate++;
                        getLog().info("Discarding duplicate ID " + id.getIdentifyingId() + " for object of class "
                                + id.getClass().getSimpleName());
                        id.setIdentifyingId(null);
                    } else {
                        throw new EnaParsingException("Genome " + genome.getName() + " (ID " + genome.getId()
                                + " contains duplicate ID " + id.getIdentifyingId() + " for object of class "
                                + id.getClass().getSimpleName());
                    }
                }
            }
        }
        return nDuplicate;
    }

    protected boolean isAllowDuplicates(Genome genome) {
        return genome.getCreationDate().before(config.getStrictDate());
    }

}
