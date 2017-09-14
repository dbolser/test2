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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.ModelUtils;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.RnaTranscript;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.sequence.SequenceTranslationException;
import uk.ac.ebi.proteome.genomebuilder.validator.GenomeValidationUncheckedException;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;

/**
 * Class to identify where proteins have internal overlaps and resolve them
 * 
 * @author dstaines
 * 
 */
public class LocationOverlapProcessor implements GenomeProcessor {

    private Log log;

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    private final EnaGenomeConfig config;
    private final DatabaseReferenceTypeRegistry registry;

    public LocationOverlapProcessor(EnaGenomeConfig config, DatabaseReferenceTypeRegistry registry) {
        this.config = config;
        this.registry = registry;
    }

    public void processGenome(Genome genome) {
        for (final GenomicComponent component : genome.getGenomicComponents()) {
            for (final Gene gene : component.getGenes()) {
                for (final Protein p : gene.getProteins()) {
                    if (ModelUtils.hasInternalOverlap(p.getLocation())) {
                        getLog().info("Protein " + p.getIdentifyingId() + " has an internal overlap: " + p.getLocation()
                                + " - attempting to resolve");
                        try {
                            final EntityLocation newLoc = ModelUtils.resolveOverlap(component, p.getLocation());
                            p.setLocation(newLoc);
                        } catch (final SequenceTranslationException e) {
                            if (p.isPseudo()) {
                                getLog().warn("Cannot translate pseudo protein " + p.getIdentifyingId()
                                        + " when resolving overlap - using external location only");
                                p.setLocation(new DelegatingEntityLocation(
                                        LocationUtils.getOuterLocation(p.getLocation()), p.getLocation().getState()));
                            } else {
                                throw e;
                            }
                        }
                    }
                }
            }
            for (final Rnagene gene : component.getRnagenes()) {
                if (ModelUtils.hasInternalOverlap(gene.getLocation())) {
                    getLog().info("RNA gene " + gene.getIdentifyingId() + " has an internal overlap: "
                            + gene.getLocation() + " - attempting to resolve");
                    final EntityLocation newLoc = ModelUtils.resolveNoncodingOverlap(component, gene.getLocation());
                    if (ModelUtils.hasInternalOverlap(newLoc)) {
                        final String msg = "RNA gene " + gene.getIdentifyingId() + " has an internal overlap: "
                                + gene.getLocation() + " - cannot resolve";
                        getLog().info(msg);
                        throw new GenomeValidationUncheckedException(msg);
                    }
                    gene.setLocation(newLoc);
                }
                for (RnaTranscript t : gene.getTranscripts()) {
                    if (ModelUtils.hasInternalOverlap(t.getLocation())) {
                        getLog().info("RNA transcript " + t.getIdentifyingId() + " has an internal overlap: "
                                + t.getLocation() + " - attempting to resolve");
                        final EntityLocation newLoc = ModelUtils.resolveNoncodingOverlap(component, t.getLocation());
                        if (ModelUtils.hasInternalOverlap(newLoc)) {
                            final String msg = "RNA transcript " + t.getIdentifyingId() + " has an internal overlap: "
                                    + t.getLocation() + " - cannot resolve";
                            getLog().info(msg);
                            throw new GenomeValidationUncheckedException(msg);
                        }
                        t.setLocation(newLoc);
                    }
                }
            }
        }
    }

}
