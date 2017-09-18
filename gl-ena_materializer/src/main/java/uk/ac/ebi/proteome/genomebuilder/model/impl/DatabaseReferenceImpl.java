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
 * File: DatabaseReference.java
 * Created by: dstaines
 * Created on: Apr 13, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.model.impl;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.util.EqualsHelper;
import uk.ac.ebi.proteome.util.HashcodeHelper;

/**
 * Bean implementation
 * 
 * @author dstaines
 * 
 */
public class DatabaseReferenceImpl extends AbstractModelComponent implements DatabaseReference {

    private static final long serialVersionUID = 1L;

    private DatabaseReferenceType databaseReferenceType = null;

    private String primaryIdentifier = null;

    private String quarternaryIdentifier = null;

    private String secondaryIdentifier = null;

    private String tertiaryIdentifier = null;

    private String description = null;

    private Double identity = null;

    private String version = null;

    private DatabaseReference source = null;

    /**
     * Create an empty reference
     */
    public DatabaseReferenceImpl() {
    }

    /**
     * @param database
     * @param identifier
     */
    public DatabaseReferenceImpl(DatabaseReferenceType database, String identifier) {
        this.databaseReferenceType = database;
        this.primaryIdentifier = identifier;
    }

    /**
     * @param database
     * @param identifier
     */
    public DatabaseReferenceImpl(DatabaseReferenceType database, String identifier, String secondaryIdentifier) {
        this.databaseReferenceType = database;
        this.primaryIdentifier = identifier;
        this.secondaryIdentifier = secondaryIdentifier;
    }

    /**
     * @return name of database
     */
    public DatabaseReferenceType getDatabaseReferenceType() {
        return this.databaseReferenceType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.genomebuilder.model.Integr8ModelComponent#getIdString
     * ()
     */
    public String getIdString() {
        return toString();
    }

    /**
     * @return identifier of entry in database
     */
    public String getPrimaryIdentifier() {
        return this.primaryIdentifier;
    }

    public String getQuarternaryIdentifier() {
        return this.quarternaryIdentifier;
    }

    public String getSecondaryIdentifier() {
        return this.secondaryIdentifier;
    }

    public String getTertiaryIdentifier() {
        return this.tertiaryIdentifier;
    }

    /**
     * @param database
     *            name of database
     */
    public void setDatabaseReferenceType(DatabaseReferenceType database) {
        this.databaseReferenceType = database;
    }

    /**
     * @param identifier
     *            identifier of entry in database
     */
    public void setPrimaryIdentifier(String identifier) {
        this.primaryIdentifier = identifier;
    }

    public void setQuarternaryIdentifier(String quarternaryIdentifier) {
        this.quarternaryIdentifier = quarternaryIdentifier;
    }

    public void setSecondaryIdentifier(String secondaryIdentifier) {
        this.secondaryIdentifier = secondaryIdentifier;
    }

    public void setTertiaryIdentifier(String tertiaryIdentifier) {
        this.tertiaryIdentifier = tertiaryIdentifier;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(getDatabaseReferenceType().toString());
        s.append(':');
        s.append(getPrimaryIdentifier());
        for (String id : new String[] { getSecondaryIdentifier(), getTertiaryIdentifier(),
                getQuarternaryIdentifier() }) {
            if (!StringUtils.isEmpty(id)) {
                s.append(',');
                s.append(id);
            }
        }
        return s.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = false;
        if (EqualsHelper.classEqual(this, obj)) {
            DatabaseReferenceImpl casted = (DatabaseReferenceImpl) obj;
            equals = (EqualsHelper.equal(this.databaseReferenceType, casted.databaseReferenceType)
                    && EqualsHelper.equal(this.primaryIdentifier, casted.primaryIdentifier)
            // use only primary to determine identity
            // && EqualsHelper.equal(this.secondaryIdentifier,
            // casted.secondaryIdentifier)
            // && EqualsHelper.equal(this.tertiaryIdentifier,
            // casted.tertiaryIdentifier) && EqualsHelper.equal(
            // this.quarternaryIdentifier, casted.quarternaryIdentifier)
            );
        }
        return equals;
    }

    @Override
    public int hashCode() {
        int hash = HashcodeHelper.SEED;
        hash = HashcodeHelper.hash(hash, databaseReferenceType);
        hash = HashcodeHelper.hash(hash, primaryIdentifier);
        // use only primary to determine identity
        // hash = HashcodeHelper.hash(hash, secondaryIdentifier);
        // hash = HashcodeHelper.hash(hash, tertiaryIdentifier);
        // hash = HashcodeHelper.hash(hash, quarternaryIdentifier);
        return hash;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference#getDescription()
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference#getIdentity()
     */
    public Double getIdentity() {
        return identity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference#isIdentityXref()
     */
    public boolean isIdentityXref() {
        return identity != null && identity == 1.0; // is this a problem?
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference#setIdentity(
     * double)
     */
    public void setIdentity(Double identity) {
        this.identity = identity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference#getVersion()
     */
    public String getVersion() {
        return this.version;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference#setVersion(java
     * .lang.String)
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public DatabaseReference getSource() {
        return source;
    }

    public void setSource(DatabaseReference source) {
        this.source = source;
    }

}
