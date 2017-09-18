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
package org.ensembl.genomeloader.genomebuilder.model;

import java.util.Date;
import java.util.List;

/**
 * Interface describing some basic information about a genome and the organism
 * to which it belongs
 *
 * @author dstaines
 *
 */
public interface GenomeInfo {

    public static final String GENOME = "GENOME";

    public static enum OrganismNameType {
        FULL, SHORT, FILE, SQL, GR, COMPARA, COMMON, STRAIN, SEROTYPE, SUBSTRAIN;
    }

    public static final String DEFAULT_SUPERREGNUM = "unknown";
    public static final String BAC_SUPERREGNUM = "bacteria";
    public static final String EUBAC_SUPERREGNUM = "eubacteria";
    public static final String EUK_SUPERREGNUM = "eukaryota";
    public static final String VIR_SUPERREGNUM = "viruses";
    public static final String ARC_SUPERREGNUM = "archaea";

    public abstract String getId();

    public abstract String getName();

    public abstract String getOrganismName(OrganismNameType nameType);

    public abstract String getDescription();

    public abstract int getTaxId();

    public abstract String getSuperregnum();

    public abstract List<String> getLineage();

    public abstract String getVersion();

    public abstract Date getCreationDate();

    public abstract Date getUpdateDate();

    public abstract String getAssemblyName();

    public abstract void setDescription(String d);

    public abstract void setVersion(String v);

    public abstract void setCreationDate(Date d);

    public abstract void setUpdateDate(Date d);

    public abstract void setOrganismName(OrganismNameType nameType, String name);

    public abstract void setAssemblyName(String assemblyName);

    public abstract void setLineage(List<String> lineage);

}
