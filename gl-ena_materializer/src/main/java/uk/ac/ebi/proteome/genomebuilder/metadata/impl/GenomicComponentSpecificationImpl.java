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
 * File: GenomicComponentSpecificationImpl.java
 * Created by: dstaines
 * Created on: May 6, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.genomebuilder.metadata.impl;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentSpecification;
import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo;

/**
 * Bean implementation of {@link GenomicComponentSpecification}
 *
 * @author dstaines
 *
 */
public class GenomicComponentSpecificationImpl implements GenomicComponentSpecification {

    private static final long serialVersionUID = -843128457524605621L;

    private String accession;

    private int type;

    private String olnRegexp = null;

    private int length = 0;

    private int geneticCode = 1;

    private boolean circular = false;

    private String description;

    private GenomeInfo genomeInfo;

    private String moleculeType;

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getOlnRegexp() {
        return olnRegexp;
    }

    public void setOlnRegexp(String olnRegexp) {
        this.olnRegexp = olnRegexp;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getGeneticCode() {
        return geneticCode;
    }

    public void setGeneticCode(int geneticCode) {
        this.geneticCode = geneticCode;
    }

    public boolean isCircular() {
        return circular;
    }

    public void setCircular(boolean circular) {
        this.circular = circular;
    }

    public GenomeInfo getGenomeInfo() {
        return genomeInfo;
    }

    public void setGenomeInfo(GenomeInfo genomeInfo) {
        this.genomeInfo = genomeInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMoleculeType() {
        return moleculeType;
    }

    public void setMoleculeType(String moleculeType) {
        this.moleculeType = moleculeType;
    }

}
