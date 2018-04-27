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

package org.ensembl.genomeloader.materializer.processors;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.EnaGenomeMaterializer;
import org.ensembl.genomeloader.materializer.EnaParsingException;
import org.ensembl.genomeloader.materializer.EnaXmlRetriever;
import org.ensembl.genomeloader.materializer.impl.EnaContigParser;
import org.ensembl.genomeloader.materializer.impl.MaterializationUncheckedException;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData.GenomicComponentType;
import org.ensembl.genomeloader.model.AssemblyElement;
import org.ensembl.genomeloader.model.AssemblySequence;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.impl.AssemblySequenceImpl;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

/**
 * Use assembly information for CON sequences to replicate assembly in an
 * Ensembl-compatible way. This involves retrieving contigs referenced by CONs
 * as additional {@link GenomicComponent}s.
 * 
 * @author dstaines
 *
 */
public class AssemblyContigProcessor implements GenomeProcessor {

    private final EnaGenomeMaterializer materializer;
    private final EnaGenomeConfig config;
    private final Log log;

    public AssemblyContigProcessor(EnaGenomeConfig config, DatabaseReferenceTypeRegistry registry,
            EnaXmlRetriever retriever) {
        this(config, new EnaContigParser(retriever, registry), null);
    }

    public AssemblyContigProcessor(EnaGenomeConfig config, EnaContigParser parser, GenomeProcessor processor) {
        this(config, new EnaGenomeMaterializer(config.getEnaEntryUrl(), parser));
    }

    public AssemblyContigProcessor(EnaGenomeConfig config, EnaGenomeMaterializer materializer) {
        this.materializer = materializer;
        this.log = LogFactory.getLog(this.getClass());
        this.config = config;
    }

    public void processGenome(Genome genome) {
        final boolean assemblyValid = assemblyValid(genome);
        // 1. build a hash of the components
        final Map<String, GenomicComponent> newComponents = CollectionUtils.createHashMap();
        log.info("Processing contigs for genome " + genome.getName());
        for (final GenomicComponent topLevel : genome.getGenomicComponents()) {
            if (config.isLoadAssembly() && assemblyValid) {
                processComponent(topLevel, newComponents);
            } else {
                processTopLevelComponent(topLevel, newComponents);
            }
        }
        // 2. add new components
        for (final GenomicComponent newComponent : newComponents.values()) {
            genome.getGenomicComponents().add(newComponent);
        }
        log.info("Finished processing contigs for genome " + genome.getName());
    }

    private void processTopLevelComponent(GenomicComponent topLevel, Map<String, GenomicComponent> newComponents) {
        topLevel.getAssemblyElements().clear();
        final GenomicComponentImpl contig = addChildComponent(topLevel, GenomicComponentType.CONTIG,
                topLevel.getAccession(), topLevel.getMetaData().getVersionedAccession());
        newComponents.put(contig.getAccession(), contig);
    }

    private boolean assemblyValid(Genome genome) {
        boolean valid = true;
        // check if contigs are referenced by one parent only (currently only do
        // one level only)
        final Set<String> kids = CollectionUtils.createHashSet();
        COMP: for (final GenomicComponent component : genome.getGenomicComponents()) {
            final Set<String> newKids = new HashSet<String>();
            for (final AssemblyElement ass : component.getAssemblyElements()) {
                if (AssemblySequence.class.isAssignableFrom(ass.getClass())) {
                    final AssemblySequence seq = (AssemblySequence) ass;
                    if (kids.contains(seq.getAccession())) {
                        log.warn("Contig " + seq.getAccession() + " is referenced by more than one component");
                        valid = false;
                        break COMP;
                    } else {
                        newKids.add(seq.getAccession());
                    }
                }
            }
            kids.addAll(newKids);
        }
        return valid;
    }

    private void processComponent(GenomicComponent component, Map<String, GenomicComponent> newComponents) {
        if (component.getAssemblyElements().size() > 0) {
            log.info("Processing contigs for genomic component " + component.getAccession());
            for (final AssemblyElement elem : component.getAssemblyElements()) {
                if (AssemblySequence.class.isAssignableFrom(elem.getClass())) {
                    final AssemblySequence seq = (AssemblySequence) elem;
                    log.info("Processing contig " + seq.getAccession() + " for genomic component "
                            + component.getAccession());
                    if (!newComponents.containsKey(seq.getAccession())) {
                        try {
                            // get the component
                            GenomicComponentMetaData md = new GenomicComponentMetaData(seq.getAccession(),
                                    component.getMetaData().getGenomeMetaData());
                            final GenomicComponent assComp = materializer.getComponent(md);
                            newComponents.put(seq.getAccession(), assComp);
                            assComp.setTopLevel(false);
                            // process it...
                            processComponent(assComp, newComponents);
                        } catch (final MaterializationUncheckedException e) {
                            throw new EnaParsingException("Could not parse ENA record " + seq.getAccession(), e);
                        }
                    }
                }
            }
            // remove sequence as we no longer need it
            component.setSequence(null);
            if (!component.isTopLevel()) {
                component.getMetaData().setComponentType(GenomicComponentType.SUPERCONTIG);
            }
        } else {
            if (component.isTopLevel()) {
                final GenomicComponentImpl contig = addChildComponent(component, GenomicComponentType.CONTIG,
                        component.getAccession(), component.getMetaData().getVersionedAccession());
                newComponents.put(contig.getAccession(), contig);
            } else {
                // treat this as a contig
                component.getMetaData().getSynonyms().add(component.getMetaData().getName());
                component.getMetaData().setName(component.getAccession() + "." + component.getMetaData().getVersion());
                component.getMetaData().setComponentType(GenomicComponentType.CONTIG);

            }
        }
    }

    /**
     * Create and attach a new 1:1 child component
     * 
     * @param component
     * @param type
     * @param accession
     * @param name
     * @return new component
     */
    private GenomicComponentImpl addChildComponent(GenomicComponent component, GenomicComponentType type,
            String accession, String name) {
        log.info("Creating contig for toplevel " + component.getAccession());
        // do some wiggling to sort out separating features and sequence
        // clone into a new component of contig
        final GenomicComponentImpl contig = cloneComponent(component, type, accession, name);
        // remove sequence
        component.setSequence(null);
        // for non-contigs, copy assembly elements over
        if (type.equals(GenomicComponentType.SUPERCONTIG)) {
            contig.getAssemblyElements().addAll(component.getAssemblyElements());
        }
        // replace with a 1:1 assembly mapping
        component.getAssemblyElements().clear();
        component.getAssemblyElements()
                .add(new AssemblySequenceImpl(
                        LocationUtils.buildLocation(1, component.getMetaData().getLength(),
                                component.getMetaData().getLength(), false, null),
                        contig.getAccession(), Integer.parseInt(contig.getMetaData().getVersion()), 1,
                        contig.getLength()));
        return contig;
    }

    private GenomicComponentImpl cloneComponent(GenomicComponent component, GenomicComponentType type, String accession,
            String name) {
        final GenomicComponentMetaData metaData = new GenomicComponentMetaData(accession,
                component.getMetaData().getGenomeMetaData());
        metaData.setComponentType(type);
        metaData.setAccession(accession);
        metaData.setName(name);
        metaData.setVersion(component.getMetaData().getVersion());
        metaData.setCircular(component.getMetaData().isCircular());
        metaData.setGeneticCode(component.getMetaData().getGeneticCode());
        metaData.setLength(component.getMetaData().getLength());
        final GenomicComponentImpl contig = new GenomicComponentImpl(metaData);
        contig.setSequence(component.getSequence());
        contig.setLength(metaData.getLength());
        contig.setAccession(accession);
        return contig;
    }

}
