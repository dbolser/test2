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

/**
 * File: GenomicComponentImpl.java
 * Created by: dstaines
 * Created on: Nov 23, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.model.AssemblyElement;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.Pseudogene;
import org.ensembl.genomeloader.model.RepeatRegion;
import org.ensembl.genomeloader.model.Rnagene;
import org.ensembl.genomeloader.model.SimpleFeature;
import org.ensembl.genomeloader.model.sequence.Sequence;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.reflection.ObjectRenderer;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author dstaines
 * 
 * 
 * 
 */
public class GenomicComponentImpl implements GenomicComponent {

    private static final long serialVersionUID = -3148372101581815598L;

    protected String accession;

    private List<AssemblyElement> assemblyElements;

    private Set<SimpleFeature> features;

    private Set<Gene> genes = null;

    @JsonIgnore
    protected Genome genome;

    protected String id;

    private int length = 0;

    private GenomicComponentMetaData metaData;

    private Set<Pseudogene> pgenes = null;

    private Set<DatabaseReference> references;

    private Set<RepeatRegion> repeats;

    private Set<Rnagene> rnagenes;

    private Sequence sequence;

    private boolean topLevel = false;

    private int type;

    public GenomicComponentImpl(GenomicComponentMetaData metadata) {
        this.metaData = metadata;
    }

    public void addDatabaseReference(DatabaseReference reference) {
        this.getDatabaseReferences().add(reference);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.GenomicComponent#addGene(uk.
     * ac .ebi.proteome.genomebuilder.model.Gene)
     */
    public void addGene(Gene gene) {
        getGenes().add(gene);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.genomebuilder.model.GenomicComponent#
     * addPseudogenes (org.ensembl.genomeloader.genomebuilder.model.Gene)
     */
    public void addPseudogene(Pseudogene gene) {
        getPseudogenes().add(gene);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ensembl.genomeloader.genomebuilder.GenomicComponent#getComponentAc()
     */
    public String getAccession() {
        return accession;
    }

    public List<AssemblyElement> getAssemblyElements() {
        if (this.assemblyElements == null) {
            this.assemblyElements = CollectionUtils.createArrayList();
        }
        return this.assemblyElements;
    }

    public Set<DatabaseReference> getDatabaseReferences() {
        if (references == null) {
            references = CollectionUtils.createHashSet();
        }
        return references;
    }

    public Set<SimpleFeature> getFeatures() {
        if (features == null) {
            features = CollectionUtils.createHashSet();
        }
        return features;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.GenomicComponent#getGenes()
     */
    public Set<Gene> getGenes() {
        if (genes == null) {
            genes = CollectionUtils.createHashSet();
        }
        return genes;
    }

    public Genome getGenome() {
        return genome;
    }

    public String getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#
     * getIdString ()
     */
    public String getIdString() {
        return getAccession();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.genomebuilder.GenomicComponent#getLength()
     */
    public int getLength() {
        return this.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.GenomicComponent#getMetaData
     * ()
     */
    public GenomicComponentMetaData getMetaData() {
        return metaData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.genomebuilder.model.GenomicComponent#
     * getPseudogenes()
     */
    public Set<Pseudogene> getPseudogenes() {
        if (pgenes == null) {
            pgenes = CollectionUtils.createHashSet();
        }
        return pgenes;
    }

    public Set<RepeatRegion> getRepeats() {
        if (repeats == null) {
            repeats = CollectionUtils.createHashSet();
        }
        return repeats;
    }

    public Set<Rnagene> getRnagenes() {
        if (rnagenes == null) {
            rnagenes = CollectionUtils.createHashSet();
        }
        return rnagenes;
    }

    public Sequence getSequence() {
        return sequence;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.genomebuilder.GenomicComponent#getType()
     */
    public int getType() {
        return type;
    }

    public String getVersionedAccession() {
        return this.getMetaData().getVersionedAccession();
    }

    public boolean isTopLevel() {
        return this.topLevel;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    /**
     * @param genes
     */
    public void setGenes(Set<Gene> genes) {
        this.genes = genes;
    }

    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setMetaData(GenomicComponentMetaData metaData) {
        this.metaData = metaData;
    }

    /**
     * @param pgenes
     */
    public void setPseudogenes(Set<Pseudogene> pgenes) {
        this.pgenes = pgenes;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ObjectRenderer.objectToString(this);
    }

}
