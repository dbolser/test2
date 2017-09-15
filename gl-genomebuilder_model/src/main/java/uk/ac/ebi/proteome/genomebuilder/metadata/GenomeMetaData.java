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
 * File: GenomeMetaData.java
 * Created by: dstaines
 * Created on: Oct 9, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.genomebuilder.metadata;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Metadata describing a genome and its components
 * 
 * @author dstaines
 * 
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class GenomeMetaData extends EntityMetaData implements GenomeInfo {

    private static final long serialVersionUID = 1L;

    private GenomeInfo info;

    private List<GenomicComponentMetaData> componentMetaData;

    // private String version;
    // private Date creationDate;
    // private Date updateDate;

    /**
     * @param id
     */
    public GenomeMetaData(String id) {
        super(GenomeInfo.GENOME, id);
        this.info = null;
    }

    /**
     * @param info
     */
    public GenomeMetaData(GenomeInfo info) {
        super(GenomeInfo.GENOME, info.getId());
        this.info = info;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getId()
     */
    public String getId() {
        return info.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getLineage()
     */
    public List<String> getLineage() {
        return info.getLineage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getName()
     */
    public String getName() {
        return info.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getOrganismName(uk.
     * ac.ebi.proteome.genomebuilder.model.GenomeInfo.OrganismNameType)
     */
    public String getOrganismName(OrganismNameType nameType) {
        return info.getOrganismName(nameType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getSuperregnum()
     */
    public String getSuperregnum() {
        return info.getSuperregnum();
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getTaxId()
     */
    public int getTaxId() {
        return info.getTaxId();
    }

    public List<GenomicComponentMetaData> getComponentMetaData() {
        if (componentMetaData == null)
            componentMetaData = CollectionUtils.createArrayList();
        return componentMetaData;
    }

    public void setInfo(GenomeInfo info) {
        this.info = info;
    }

    public void setOrganismName(OrganismNameType nameType, String name) {
        info.setOrganismName(nameType, name);
    }

    /**
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getVersion()
     * @return version
     */
    public String getVersion() {
        return info.getVersion();
    }

    /**
     * @return creation date
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getCreationDate()
     */
    public Date getCreationDate() {
        return info.getCreationDate();
    }

    /**
     * @return update date
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getUpdateDate()
     */
    public Date getUpdateDate() {
        return info.getUpdateDate();
    }

    /**
     * @return assembly name
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getAssemblyName()
     */
    public String getAssemblyName() {
        return info.getAssemblyName();
    }

    /**
     * @param v
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#setVersion(java.lang.String)
     */
    public void setVersion(String v) {
        info.setVersion(v);
    }

    /**
     * @param d
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#setCreationDate(java.util.Date)
     */
    public void setCreationDate(Date d) {
        info.setCreationDate(d);
    }

    /**
     * @param d
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#setUpdateDate(java.util.Date)
     */
    public void setUpdateDate(Date d) {
        info.setUpdateDate(d);
    }

    /**
     * @param assemblyName
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#setAssemblyName(java.lang.String)
     */
    public void setAssemblyName(String assemblyName) {
        info.setAssemblyName(assemblyName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getDescription()
     */
    public String getDescription() {
        return info.getDescription();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#setDescription(java.
     * lang.String)
     */
    public void setDescription(String d) {
        info.setDescription(d);
    }

    public void setLineage(List<String> lineage) {
        info.setLineage(lineage);
    }

}
