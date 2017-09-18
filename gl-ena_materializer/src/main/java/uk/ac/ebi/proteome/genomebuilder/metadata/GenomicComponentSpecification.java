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
 * File: GenomicComponentSpecification.java
 * Created by: dstaines
 * Created on: May 6, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.genomebuilder.metadata;

import java.io.Serializable;
import java.util.Date;

import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo;

/**
 * Simple interface for storing information about a component
 *
 * @author dstaines
 */
public interface GenomicComponentSpecification extends Serializable {

	public abstract GenomeInfo getGenomeInfo();

	public abstract void setGenomeInfo(GenomeInfo genome);

	public abstract int getLength();

	public abstract void setLength(int length);

	public abstract int getType();

	public abstract void setType(int type);

	public abstract String getOlnRegexp();

	public abstract void setOlnRegexp(String olnRegexp);

	public abstract int getGeneticCode();

	public abstract void setGeneticCode(int geneticCode);

	public abstract boolean isCircular();

	public abstract void setCircular(boolean circular);

	public abstract String getAccession();

	public abstract void setAccession(String accession);

	public String getDescription();

	public void setDescription(String description);

	public String getMoleculeType();

	public void setMoleculeType(String moleculeType);
	
}
