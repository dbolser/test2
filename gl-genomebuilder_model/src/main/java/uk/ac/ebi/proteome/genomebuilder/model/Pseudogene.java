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

/**
 * Interface combining gene and location information for annotated and derived
 * pseudogenes
 *
 * @author dstaines
 */
public interface Pseudogene extends AnnotatedGene, Locatable, CrossReferenced,
		Identifiable {

	public enum PseudogeneType {
		EMBL_ANNOTATION, UNIPROT_ANNOTATION, CDS_MISSING, CDS_UNTRANSLATABLE;

		public static PseudogeneType valueOf(int i) {
			return PseudogeneType.values()[i];
		}

	}

	/**
	 * The name of a gene is the agreed name used for convenience in identifying
	 * it
	 *
	 * @return agreed gene name
	 */
	public String getName();

	/**
	 * The name of a gene is the agreed name used for convenience in identifying
	 * it
	 *
	 * @param name
	 *            agreed gene name
	 */
	public void setName(String name);

	public void setType(PseudogeneType type);

	public PseudogeneType getType();
	
	public String getDescription();

}
