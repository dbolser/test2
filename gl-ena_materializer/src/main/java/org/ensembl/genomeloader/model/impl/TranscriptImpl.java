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
 * File: TranscriptImpl.java
 * Created by: dstaines
 * Created on: Oct 4, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model.impl;

import java.util.Set;

import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.EntityLocation;
import org.ensembl.genomeloader.model.Operon;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.Transcript;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author dstaines
 *
 */
public class TranscriptImpl implements Transcript {

    private static final long serialVersionUID = -4842634582846697240L;
    private String name;
    private String promoter;
    @JsonIgnore
    private Set<Protein> proteins;
    private Operon operon;
    private EntityLocation location;
    private Set<DatabaseReference> databaseReferences;
    private String coTranscribedUnit;

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
     * @see org.ensembl.genomeloader.genomebuilder.model.Transcript#getId()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.Transcript#getProteins()
     */
    public Set<Protein> getProteins() {
        if (this.proteins == null) {
            this.proteins = CollectionUtils.createHashSet();
        }
        return this.proteins;
    }

    /**
     * @param proteins
     */
    public void setProteins(Set<Protein> proteins) {
        this.proteins = proteins;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.Locatable#getLocation()
     */
    public EntityLocation getLocation() {
        return this.location;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Locatable#setLocation(org.biojavax
     * .bio.seq.EntityLocation)
     */
    public void setLocation(EntityLocation location) {
        this.location = location;
    }

    /**
     * @return references attached to this transcript
     */
    public Set<DatabaseReference> getDatabaseReferences() {
        if (this.databaseReferences == null) {
            databaseReferences = CollectionUtils.createHashSet();
        }
        return this.databaseReferences;
    }

    /**
     * @param databaseReferences
     */
    public void setDatabaseReferences(Set<DatabaseReference> databaseReferences) {
        this.databaseReferences = databaseReferences;
    }

    public void addDatabaseReference(DatabaseReference reference) {
        getDatabaseReferences().add(reference);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Transcript#addProtein(uk.ac.ebi.
     * proteome.genomebuilder.model.Protein)
     */
    public void addProtein(Protein protein) {
        getProteins().add(protein);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#getIdString(
     * )
     */
    public String getIdString() {
        return name == null ? identifyingId : name;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Transcript#addOperon(uk.ac.ebi.
     * proteome.genomebuilder.model.Operon)
     */
    public void setOperon(Operon operon) {
        this.operon = operon;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.Transcript#getOperons()
     */
    public Operon getOperon() {
        return this.operon;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ensembl.genomeloader.genomebuilder.model.Transcript#getPromoter()
     */
    public String getPromoter() {
        return promoter;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Transcript#setPromoter(java.lang.
     * String)
     */
    public void setPromoter(String promoter) {
        this.promoter = promoter;
    }

    public String getCoTranscribedUnit() {
        return this.coTranscribedUnit;
    }

    public void setCoTranscribedUnit(String string) {
        this.coTranscribedUnit = string;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getIdString());
        s.append('[');
        s.append("promoter=" + getPromoter());
        s.append(",cotranscribedunit=" + getCoTranscribedUnit());
        s.append(",location=" + LocationUtils.locationToEmblFormat(getLocation()));
        s.append(']');
        return s.toString();
    }

}
