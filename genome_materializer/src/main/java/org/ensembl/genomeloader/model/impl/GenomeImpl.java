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
 * File: Genome.java
 * Created by: dstaines
 * Created on: Sep 24, 2008
 * CVS:  $$
 */
package org.ensembl.genomeloader.model.impl;

import java.util.List;
import java.util.Set;

import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * @author dstaines
 *
 */
public class GenomeImpl implements Genome {

    private static final long serialVersionUID = -2350154161095127330L;
    private final GenomeMetaData metadata;
    private List<GenomicComponent> genomicComponents;
    private Set<DatabaseReference> references;

    public GenomeImpl(GenomeMetaData metadata) {
        this.metadata = metadata;
    }

    public void addGenomicComponent(GenomicComponent component) {
        getGenomicComponents().add(component);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Genome#getGenomicComponents(
     * )
     */
    public List<GenomicComponent> getGenomicComponents() {
        if (genomicComponents == null) {
            genomicComponents = CollectionUtils.createArrayList();
        }
        return genomicComponents;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#
     * getIdString( )
     */
    public String getIdString() {
        return metadata.getId();
    }

    public Set<DatabaseReference> getDatabaseReferences() {
        if (references == null) {
            references = CollectionUtils.createHashSet();
        }
        return references;
    }

    public void addDatabaseReference(DatabaseReference reference) {
        this.getDatabaseReferences().add(reference);
    }

    @Override
    public GenomeMetaData getMetaData() {
        return metadata;
    }

    @Override
    public String getName() {
        return metadata.getName();
    }

    @Override
    public String getId() {
        return metadata.getId();
    }

}
