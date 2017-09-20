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
package org.ensembl.genomeloader.metadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Metadata describing a genome and its components
 * 
 * @author dstaines
 * 
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class GenomeMetaData {

    public static final java.lang.String DEFAULT_SUPERREGNUM = "unknown";
    public static final java.lang.String BAC_SUPERREGNUM = "bacteria";
    public static final java.lang.String EUBAC_SUPERREGNUM = "eubacteria";
    public static final java.lang.String EUK_SUPERREGNUM = "eukaryota";
    public static final java.lang.String VIR_SUPERREGNUM = "viruses";
    public static final java.lang.String ARC_SUPERREGNUM = "archaea";

    public static enum OrganismNameType {
        FULL, SHORT, FILE, SQL, GR, COMPARA, COMMON, STRAIN, SEROTYPE, SUBSTRAIN;
    }

    private final String id;
    private final String name;
    private final Map<OrganismNameType, String> organismNames = new HashMap<>();
    private final int taxId;
    private List<String> lineage;
    private String version;
    private String assemblyName;
    private Date creationDate;
    private Date updateDate;
    private String description;
    private String superregnum;
    private List<GenomicComponentMetaData> componentMetaData = new ArrayList<>();

    public GenomeMetaData(String id, String name, int taxId) {
        this.id = id;
        this.name = name;
        this.taxId = taxId;
    }

    public List<String> getLineage() {
        return lineage;
    }

    public void setLineage(List<String> lineage) {
        this.lineage = lineage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAssemblyName() {
        return assemblyName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<GenomicComponentMetaData> getComponentMetaData() {
        return componentMetaData;
    }

    public void setComponentMetaData(List<GenomicComponentMetaData> componentMetaData) {
        this.componentMetaData = componentMetaData;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<OrganismNameType, String> getOrganismNames() {
        return organismNames;
    }

    public int getTaxId() {
        return taxId;
    }

    public String getOrganismName(OrganismNameType type) {
        return this.organismNames.get(type);
    }

    public void setOrganismName(OrganismNameType type, String name) {
        this.organismNames.put(type, name);
    }

    public String getSuperregnum() {
        return superregnum;
    }

    public void setSuperregnum(String superregnum) {
        this.superregnum = superregnum;
    }
}
