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
 * File: GenomicComponentMetaData.java
 * Created by: dstaines
 * Created on: Mar 9, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.metadata;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;

import org.ensembl.genomeloader.metadata.impl.DefaultGenomicComponentDescriptionHandler;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ResolverMetaData describing a genomic component, and optionally its
 * associated CDS meta data. Also contains some basic properties of the
 * component (scope, type, superregnum) to allow filtering.
 * 
 * @author dstaines
 * 
 */
public class GenomicComponentMetaData {

    public static enum GenomicComponentType {
        CHROMOSOME(1), CONTIG(4), PLASMID(2), SUPERCONTIG(3);
        int rank;

        GenomicComponentType(int rank) {
            this.rank = rank;
        }

        public int getRank() {
            return rank;
        }
    }

    /**
     * Comparator for comparing two instances of
     * {@link GenomicComponentMetaData} based on type and then rank
     */
    public static final Comparator<GenomicComponentMetaData> COMPONENT_COMPARATOR = new Comparator<GenomicComponentMetaData>() {

        public int compare(GenomicComponentMetaData o1, GenomicComponentMetaData o2) {
            if (o1.getComponentType() != o2.getComponentType()) {
                // compare type rank
                return Integer.valueOf(o1.getComponentType().getRank())
                        .compareTo(Integer.valueOf(o2.getComponentType().getRank()));
            } else {
                // compare length
                return Integer.valueOf(o1.getLength()).compareTo(Integer.valueOf(o2.getLength()));
            }
        }

    };;

    public static final int NULL_GENETIC_CODE = -1;

    public static final String PROTEIN_SOURCE_TYPE = "protSrc";

    private static final long serialVersionUID = 1L;

    private String accession;

    private boolean circular = false;

    private GenomicComponentType componentType;

    private boolean con;

    private Date creationDate;

    private String description;

    @JsonIgnore
    private GenomicComponentDescriptionHandler descriptionHandler = new DefaultGenomicComponentDescriptionHandler();

    private int geneticCode = 1;

    private int length = 0;

    private String masterAccession;

    private String moleculeType;

    private String name;

    private String olnRegexp = null;

    private Set<String> synonyms;

    private int type;

    private Date updateDate;

    private String version;

    private String versionedAccession;
    
    @JsonIgnore
    private GenomeMetaData genomeMetaData;

    public GenomicComponentMetaData(String accession, GenomeMetaData genomeMetaData) {
        this.accession = accession;
        this.genomeMetaData = genomeMetaData;
    }

    public String getAccession() {
        return accession;
    }

    public GenomicComponentType getComponentType() {
        return componentType;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return description;
    }

    public GenomicComponentDescriptionHandler getDescriptionHandler() {
        return descriptionHandler;
    }

    public int getGeneticCode() {
        return geneticCode;
    }

    public int getLength() {
        return length;
    }

    public String getMasterAccession() {
        return masterAccession;
    }

    public String getMoleculeType() {
        return moleculeType;
    }

    public String getName() {
        return name;
    }

    public String getOlnRegexp() {
        return olnRegexp;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public int getType() {
        return type;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionedAccession() {
        return versionedAccession;
    }

    public boolean isCircular() {
        return circular;
    }

    public boolean isCon() {
        return con;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public void setCircular(boolean circular) {
        this.circular = circular;
    }

    public void setComponentType(GenomicComponentType componentType) {
        this.componentType = componentType;
    }

    public void setCon(boolean con) {
        this.con = con;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDescriptionHandler(GenomicComponentDescriptionHandler descriptionHandler) {
        this.descriptionHandler = descriptionHandler;
    }

    public void setGeneticCode(int geneticCode) {
        this.geneticCode = geneticCode;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setMasterAccession(String masterAccession) {
        this.masterAccession = masterAccession;
    }

    public void setMoleculeType(String moleculeType) {
        this.moleculeType = moleculeType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOlnRegexp(String olnRegexp) {
        this.olnRegexp = olnRegexp;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setVersionedAccession(String versionedAccession) {
        this.versionedAccession = versionedAccession;
    }

    public GenomeMetaData getGenomeMetaData() {
        return genomeMetaData;
    }

}
