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
 * File: Pseudogene.java
 * Created by: dstaines
 * Created on: Nov 29, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.model;

import java.util.Set;

/**
 * Interface combining gene name and location information for annotated and derived
 * rnagenes
 *
 * @author hornead
 */
public interface Rnagene extends AnnotatedGene, Locatable, CrossReferenced, Identifiable {

	public static final String RRNA_BIOTYPE = "rRNA";
	public static final String TRNA_BIOTYPE = "tRNA";
	
	/**
	 * Is pseudogene ?
	 *
	 * @return is pseudogene ?
	 */
	boolean isPseudogene();

    /**
	 * The name of a gene is the agreed name used for convenience in identifying
	 * it
	 *
	 * @return agreed gene name
	 */
	String getName();

	/**
	 * The description of a gene.
	 *
	 * @return gene description
	 */
	String getDescription();
	void setDescription(String description);

	/**
	 * The biotype (an Ensembl concept) of an Rnagene
	 */
	String getBiotype();

	/**
	 * Returns the type of analysis applied to the Rnagene
	 */
	String getAnalysis();
	
	/**
	 * @return transcripts included with gene
	 */
	public Set<RnaTranscript> getTranscripts();

	public void addTranscript(RnaTranscript transcript);

	
}
