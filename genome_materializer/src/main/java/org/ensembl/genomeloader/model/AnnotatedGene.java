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
 * File: AnnotatedGene.java
 * Created by: dstaines
 * Created on: Mar 8, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model;

import java.util.List;
import java.util.Map;

/**
 * an annotated gene is a collection of gene names and identifiers Integr8 genes
 * (in the Integr8 model) will extend AnnotatedGene. Additionally, other types
 * of objects (for example, ProteinDatabaseEntries) can be associated with gene
 * annotations. These annotations will be used to determine and annotate the
 * Integr8 gene objects
 *
 * @author dstaines
 */
public interface AnnotatedGene extends Integr8ModelComponent, Identifiable {

	/**
	 * Sets of gene names for this gene, arranged by type
	 *
	 * @return map keyed by name type
	 */
	public abstract Map<GeneNameType, List<GeneName>> getNameMap();

	/**
	 * @return number of names
	 */
	public abstract int getNameCount();

	/**
	 * merge the names of two 2 annotated genes into one NameMap
	 *
	 * @param annotatedGene
	 *            to merge into this
	 */
	public abstract void addAnnotatedGene(AnnotatedGene annotatedGene);

	/**
	 * Add a gene name to this gene
	 *
	 * @param name
	 *            e.g. source
	 */
	public abstract void addGeneName(GeneName name);

}
