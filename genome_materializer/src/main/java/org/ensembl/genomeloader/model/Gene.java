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
 * File: Gene.java
 * Created by: dstaines
 * Created on: Mar 8, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model;

import java.util.Set;

/**
 * Interface associating a named gene with a given location and one or more
 * protein coding regions
 *
 * @author dstaines
 */
public interface Gene extends AnnotatedGene, Locatable, CrossReferenced, Identifiable {

    public final static String PSEUDOGENE = "pseudogene";
    public final static String PROTEIN_CODING = "protein_coding";
    
	/**
	 * @return agreed gene name used for convenience in identifying it
	 */
	public String getName();

	public void setName(String name);

	/**
	 * @return accession of master protein encoded by this gene
	 */
	public String getUniprotKbAc();

	public void setUniprotKbAc(String ac);

	/**
	 * @return proteins encoded by gene
	 */
	public Set<Protein> getProteins();

	public void addProtein(Protein protein);

	/**
	 * @return stable public identifier
	 */
	public Long getPublicId();

	/**
	 * @param id stable public identifier
	 */
	public void setPublicId(Long id);

	/**
	 * @return brief description of the gene
	 */
	public String getDescription();
	
	/**
	 * @return type of gene
	 */
	public String getBiotype();
	
	public default boolean isPseudo() {
	    return PSEUDOGENE.equals(getBiotype());
	}

}
