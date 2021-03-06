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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichLocation.Strand;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.model.EntityLocation;
import org.ensembl.genomeloader.model.EntityLocationException;
import org.ensembl.genomeloader.model.EntityLocationInsertion;
import org.ensembl.genomeloader.model.EntityLocationModifier;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.Integr8ModelComponent;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.validator.GenomeValidationException;
import org.ensembl.genomeloader.validator.GenomeValidator;

/**
 * {@link GenomeValidator} for checking if applied location modifiers are valid
 * and locations on linear components are correct.
 * 
 * If {@link EnaGenomeConfig#isSkipBrokenLocations()} is set, genes with
 * incorrect locations will be removed in the interests of pragmatism
 * 
 * @author dstaines
 * 
 */
public class EntityLocationValidator implements GenomeValidator {

    private Log log;

    private final EnaGenomeConfig config;

    public EntityLocationValidator(EnaGenomeConfig config) {
        this.config = config;
    }

    protected Log getLog() {
        if (log == null)
            log = LogFactory.getLog(this.getClass());
        return log;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.genomebuilder.validator.GenomeValidator#
     * validateGenome (org.ensembl.genomeloader.genomebuilder.model.Genome)
     */
    public void validateGenome(Genome genome) throws GenomeValidationException {
        for (GenomicComponent component : genome.getGenomicComponents()) {
            Iterator<Gene> geneI = component.getGenes().iterator();
            while (geneI.hasNext()) {
                Gene gene = geneI.next();
                try {
                    for (Protein protein : gene.getProteins()) {
                        EntityLocation location = protein.getLocation();
                        validateLocation(protein, location, component.getMetaData().isCircular());
                    }
                } catch (GenomeValidationException e) {
                    if (config.isSkipBrokenLocations()) {
                        getLog().warn("Removing gene " + gene.getIdString() + " due to broken location:" + e.getMessage());
                        geneI.remove();
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Check whether the location has correct start/end and whether all
     * modifications are valid
     * 
     * @param entity
     *            biological entity to check (e.g. Protein)
     * @param location
     *            location of entity
     * @param isCircular
     *            true if component is circular
     * @throws GenomeValidationException
     */
    protected static void validateLocation(Integr8ModelComponent entity, EntityLocation location, boolean isCircular)
            throws GenomeValidationException {
        if (!isCircular) {
            if (location.getMin() > location.getMax()) {
                throw new GenomeValidationException(
                        "Location " + location.toString() + " has start>end on linear component");
            }
        }
        for (EntityLocationInsertion insertion : location.getInsertions()) {
            validateModification(insertion, entity, location);
        }
        for (EntityLocationException exception : location.getExceptions()) {
            validateModification(exception, entity, location);
        }
    }

    /**
     * Check whether the modification lies within the location
     * 
     * @param modifier
     *            location modifier to check
     * @param entity
     *            to check (e.g. Protein)
     * @param location
     *            of entity
     * @throws GenomeValidationException
     */
    protected static void validateModification(EntityLocationModifier modifier, Integr8ModelComponent entity,
            EntityLocation location) throws GenomeValidationException {
        RichLocation modLoc = LocationUtils.buildSimpleLocation(modifier.getStart(), modifier.getStop(),
                location.getStrand() == Strand.NEGATIVE_STRAND);
        if (!LocationUtils.contains(location, modLoc)) {
            throw new GenomeValidationException("Modifier " + modifier + " for protein " + entity.getIdString()
                    + " lies outside location " + location);
        } else if (Protein.class.isAssignableFrom(entity.getClass())) {
            if (location.getStrand() == Strand.NEGATIVE_STRAND) {
                if (location.getMin() == modLoc.getMax()) {
                    throw new GenomeValidationException("Modifier " + modifier + " for protein " + entity.getIdString()
                            + " lies outside coding sequence for " + location);
                }
            } else {
                if (location.getMax() == modLoc.getMin()) {
                    throw new GenomeValidationException("Modifier " + modifier + " for protein " + entity.getIdString()
                            + " lies outside coding sequence for " + location);
                }
            }
        }
    }

}
