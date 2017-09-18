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
 * File: ProteinImpl.java
 * Created by: dstaines
 * Created on: Oct 4, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.model.impl;

import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReference;
import org.ensembl.genomeloader.genomebuilder.model.EntityLocation;
import org.ensembl.genomeloader.genomebuilder.model.Protein;
import org.ensembl.genomeloader.genomebuilder.model.ProteinFeature;
import org.ensembl.genomeloader.genomebuilder.model.Transcript;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Simple bean implementation of a protein
 *
 * @author dstaines
 *
 */
public class ProteinImpl implements Protein {

    private static final long serialVersionUID = 1504915877931035757L;
    private Set<DatabaseReference> databaseReferences;
    private Set<ProteinFeature> proteinFeatures;

    private EntityLocation location;
    private String name;
    private Set<Transcript> transcripts;
    private String uniprotKbId;
    private int codonStart = 1;
    private boolean pseudo = false;

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.CrossReferenced#
     * addDatabaseReference(org.ensembl.genomeloader.genomebuilder.model.
     * DatabaseReference)
     */
    public void addDatabaseReference(DatabaseReference reference) {
        getDatabaseReferences().add(reference);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.CrossReferenced#
     * getDatabaseReferences()
     */
    public Set<DatabaseReference> getDatabaseReferences() {
        if (this.databaseReferences == null) {
            this.databaseReferences = CollectionUtils.createHashSet();
        }
        return this.databaseReferences;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.Protein#getProteinFeatures()
     */
    public Set<ProteinFeature> getProteinFeatures() {
        if (this.proteinFeatures == null) {
            this.proteinFeatures = CollectionUtils.createHashSet();
        }
        return this.proteinFeatures;
    }

    public EntityLocation getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public String getUniprotKbId() {
        return this.uniprotKbId;
    }

    public void setDatabaseReferences(Set<DatabaseReference> databaseReferences) {
        this.databaseReferences = databaseReferences;
    }

    public void setLocation(EntityLocation location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUniprotKbId(String uniprotKbId) {
        this.uniprotKbId = uniprotKbId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#getIdString(
     * )
     */
    public String getIdString() {
        return getUniprotKbId() + ":[" + getName() + "]";
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Protein#addProteinFeature(uk.ac.
     * ebi.proteome.genomebuilder.model.ProteinFeature)
     */
    public void addProteinFeature(ProteinFeature feature) {
        getProteinFeatures().add(feature);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Protein#addTranscript(uk.ac.ebi.
     * proteome.genomebuilder.model.Transcript)
     */
    public void addTranscript(Transcript transcript) {
        getTranscripts().add(transcript);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.Protein#getTranscripts()
     */
    public Set<Transcript> getTranscripts() {
        if (transcripts == null)
            transcripts = CollectionUtils.createHashSet();
        return transcripts;
    }

    private String identifyingId;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Identifiable#getIdentifyingId()
     */
    public String getIdentifyingId() {
        return identifyingId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Identifiable#setIdentifyingId(java
     * .lang.String)
     */
    public void setIdentifyingId(String identifyingId) {
        this.identifyingId = identifyingId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.Protein#getCodonStart()
     */
    public int getCodonStart() {
        return codonStart;
    }

    /**
     * @param codonStart
     *            phase in which to translate from
     */
    public void setCodonStart(int codonStart) {
        this.codonStart = codonStart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.genomebuilder.model.Protein#isPseudo()
     */
    public boolean isPseudo() {
        return pseudo;
    }

    public void setPseudo(boolean pseudo) {
        this.pseudo = pseudo;
    }

}
