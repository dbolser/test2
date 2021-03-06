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

package org.ensembl.genomeloader.validator.impl;

import java.util.Set;

import org.ensembl.genomeloader.metadata.GenomicComponentMetaData.GenomicComponentType;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.validator.GenomeValidationException;
import org.ensembl.genomeloader.validator.GenomeValidator;

/**
 * Validator that checks the genome does not mix chromosome/plasmid and
 * supercontig coord systems. This is used to catch issues where a genome
 * contains supercontig as well as chromosome/plasmid systems. Not run by
 * default.
 * 
 * @author dstaines
 * 
 */
public class MixedCoordSystemValidator implements GenomeValidator {

    public void validateGenome(Genome genome) throws GenomeValidationException {
        Set<GenomicComponentType> types = CollectionUtils.createHashSet(genome.getGenomicComponents().size());
        for (GenomicComponent component : genome.getGenomicComponents()) {
            if (component.isTopLevel()) {
                types.add(component.getMetaData().getComponentType());
            }
        }
        if (types.contains(GenomicComponentType.SUPERCONTIG)
                && (types.contains(GenomicComponentType.PLASMID) || types.contains(GenomicComponentType.CHROMOSOME))) {
            throw new GenomeValidationMixedCoordException(
                    "Genome " + genome.getName() + " mixes supercontig and chromosome/plasmid components");
        }
    }

}
