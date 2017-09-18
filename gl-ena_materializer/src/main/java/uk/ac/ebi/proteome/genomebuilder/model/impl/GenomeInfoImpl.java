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
 * File: GenomeImpl.java
 * Created by: dstaines
 * Created on: Sep 24, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.genomebuilder.model.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo;
import uk.ac.ebi.proteome.util.EqualsHelper;
import uk.ac.ebi.proteome.util.HashcodeHelper;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Bean representing information about the genome to which genomic components
 * belong
 * 
 * @author dstaines
 * 
 */

public class GenomeInfoImpl implements Serializable, GenomeInfo {

    private static final long serialVersionUID = 6202561010779989884L;
    private final String id;
    private final Map<OrganismNameType, String> organismNames;
    private final int taxId;
    private final String superregnum;
    private List<String> lineage;
    private final String name;
    private String version;
    private String assemblyName;
    private Date creationDate;
    private Date updateDate;
    private String description;

    /**
     * @param id
     * @param taxId
     * @param proteomeName
     * @param superregnum
     * @param scope
     */
    public GenomeInfoImpl(String id, int taxId, String proteomeName, String superregnum) {
        this.id = id;
        this.name = proteomeName;
        this.taxId = taxId;
        this.superregnum = superregnum;
        this.organismNames = CollectionUtils.createHashMap();
    }

    public GenomeInfoImpl(GenomeInfo info) {
        this(info.getId(), info.getTaxId(), info.getName(), info.getSuperregnum());
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.impl.Genome#getId()
     */
    public String getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.impl.Genome#getOrganism()
     */
    public String getOrganismName(OrganismNameType type) {
        return organismNames.get(type);
    }

    public void setOrganismName(OrganismNameType type, String name) {
        if (!StringUtils.isEmpty(name))
            organismNames.put(type, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.impl.Genome#getTaxId()
     */
    public int getTaxId() {
        return taxId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getSuperregnum()
     */
    public String getSuperregnum() {
        return superregnum;
    }

    @Override
    public boolean equals(Object obj) {
        return (EqualsHelper.classEqual(obj, this) && ((GenomeInfo) obj).getId().equals(getId()));
    }

    @Override
    public int hashCode() {
        return HashcodeHelper.hash(HashcodeHelper.SEED, getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo#getLineage()
     */
    public List<String> getLineage() {
        if (lineage == null)
            lineage = CollectionUtils.createArrayList();
        return lineage;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date date) {
        this.creationDate = date;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    /**
     * @return the assemblyName
     */
    public String getAssemblyName() {
        return assemblyName;
    }

    /**
     * @param assemblyName
     *            the assemblyName to set
     */
    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLineage(List<String> lineage) {
        this.lineage = lineage;
    }

}
